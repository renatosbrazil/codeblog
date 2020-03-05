package br.com.rrodrigues.scriptgen.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.rrodrigues.scriptgen.exception.BusinessException;
import br.com.rrodrigues.scriptgen.service.UserService;

@Controller
public class UserController {
	
	private static final String TELA_PRINCIPAL = "tela-principal";
	
	@Autowired
	private UserService userService;
	
    @RequestMapping("/")
    public String index() {
        return TELA_PRINCIPAL;
    }
	
	/**
	 * Metodo responsavel por gerar os scripts.
	 * 
	 * @throws IOException 
	 * 
	 */
	@PostMapping("/gerar")
	public String gerarScripts(@RequestParam("ntabela") String ntabela, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException 
	{
		String msgErro = "";
		String nomeArquivo = "";
		
		boolean gerou = false;
		
		try {
			nomeArquivo = file.getOriginalFilename();

			msgErro = userService.verificarArquivo(file, redirectAttributes, nomeArquivo); // Verifica o arquivo importado
			
			if (msgErro != "") { // Verificar se veio algum msg de erro
				redirectAttributes.addFlashAttribute("messageErro", msgErro);
				return "redirect:/";
			}
			
			userService.confirmarUpload(file); // Faz o upload do arquivo
			
			gerou = userService.confirmarGerecaoScript(nomeArquivo, ntabela); // Converte o arquivo XLSX para TXT
			
			if(gerou) {
				redirectAttributes.addFlashAttribute("messageOK", "Scripts gerados com sucesso com base no arquivo: " + nomeArquivo);
				return "redirect:/";
			} else {
				redirectAttributes.addFlashAttribute("messageErro", "Não foi possível gerar os scripts. Entre em contato com o administrador.");
				return "redirect:/";
			}
		} catch (BusinessException e) {
			redirectAttributes.addFlashAttribute("messageErro", e.getMessage());
		}
		return "redirect:/";
	}

}
