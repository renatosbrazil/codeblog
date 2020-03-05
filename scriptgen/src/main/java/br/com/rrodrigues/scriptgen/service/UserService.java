package br.com.rrodrigues.scriptgen.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.rrodrigues.scriptgen.exception.BusinessException;

public interface UserService 
{
	String verificarArquivo(MultipartFile file, RedirectAttributes mensagem, String nomeArquivo) throws BusinessException;
	
	Boolean confirmarUpload(MultipartFile file) throws BusinessException; 
	
	Boolean confirmarGerecaoScript(String nomeArquivo, String ntabela) throws BusinessException; 
}
