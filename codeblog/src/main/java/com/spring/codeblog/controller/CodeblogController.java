package com.spring.codeblog.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.jar.Attributes;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.codeblog.model.Post;
import com.spring.codeblog.service.CodeblogService;
import com.spring.codeblog.util.Paginas;

@Controller
public class CodeblogController {

	@Autowired
	CodeblogService codeblogService;
	
	
	@RequestMapping("/posts")
	public ModelAndView getPosts() {
		ModelAndView mv = new ModelAndView(Paginas.PAGINA_POST); //Pagina que sera direcionado
 
		List<Post> posts = codeblogService.findAll();
		if(posts.isEmpty() ) { //verifica se retornou algum post do banco
		 	Post post = new Post();
			post.setAutor("Nenhum documento cadastrado"); 
			mv.addObject("posts", post);
			
		}else {
			mv.addObject("posts",posts); //Manda o objeto que retornou do banco para view
		}
		
		return mv;
	}
	@RequestMapping(value ="/post/{id}", method=RequestMethod.GET)
	public ModelAndView getPostDatails(@PathVariable("id")long id) {
		ModelAndView mv = new ModelAndView(Paginas.PAGINA_DETAILS); //Pagina que sera direcionado
		Post post = codeblogService.findById(id);
		mv.addObject("post",post); //Manda o objeto para view
		return mv;
	}
	@RequestMapping(value = "/newpost", method=RequestMethod.GET)
	public String getPostForm() {
		return Paginas.PAGINA_POSTFORM;
	}
	
	
	@RequestMapping(value = "/newpost", method=RequestMethod.POST)
	public String savePost(@Valid Post post, BindingResult result, RedirectAttributes attributes ) {
		if (result.hasErrors()) {
			String mensagem="verifique se os campos obrigatorios foram preenchidos!";
			attributes.addFlashAttribute("mensagem",mensagem);
		//	attributes.addFlashAttribute(s:"mensagem", o:"verifique se os campos obrigatorios foram preenchidos!");
			return "redirect:/newpost";
		}
		post.setData(LocalDate.now());
		codeblogService.save(post);
		return "redirect:/posts";
		
		
		
		
		
		
	}
	@RequestMapping(value ="/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable("id")long id) {
		ModelAndView mv = new ModelAndView(Paginas.PAGINA_DELETE); //Pagina que sera direcionado
		Post post = codeblogService.delete(id);
		mv.addObject("post",post); //Manda o objeto para view
		return "redirect:/posts";
	}
 
	
}
