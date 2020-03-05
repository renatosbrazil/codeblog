package br.com.rrodrigues.scriptgen.service;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.rrodrigues.scriptgen.exception.BusinessException;

@Service
public class UserServiceImpl implements UserService
{
	
	// Caminho para a MTTSRV34
	// private static final String PASTA_ARQUIVO_IMPORTADO = "C:\\Users\\suporte\\Desktop\\Renascript\\oiatende\\arquivos_importados\\";
	// private static final String PASTA_SCRIPT_UPDATE = "C:\\Users\\suporte\\Desktop\\Renascript\\oiatende\\script_update\\";
	// private static final String PASTA_SCRIPT_ROLLBACK = "C:\\Users\\suporte\\Desktop\\Renascript\\oiatende\\script_rollback\\";
	// private static final String PASTA_SCRIPT_VALIDACAO = "C:\\Users\\suporte\\Desktop\\Renascript\\oiatende\\script_validacao\\";

	// Caminho para local
	private static final String PASTA_ARQUIVO_IMPORTADO = "C:\\oiatende\\arquivos_importados\\";
	private static final String PASTA_SCRIPT_UPDATE = "C:\\oiatende\\script_update\\";
	private static final String PASTA_SCRIPT_ROLLBACK = "C:\\oiatende\\script_rollback\\";
	private static final String PASTA_SCRIPT_VALIDACAO = "C:\\oiatende\\script_validacao\\";
	
	/**
	 * Metodo responsavel por validar o arquivo.
	 * 
	 * @throws BusinessException
	 * 
	 */
	@Override
	public String verificarArquivo(MultipartFile file, RedirectAttributes mensagem, String nomeArquivo) throws BusinessException 
	{
		String msgErro = "";
		long tamanhoArquivo = 0;
		long tamanhoArquivoLimite = 16777216;

		try {
			if (file.isEmpty()) { // Verifica se importou algum arquivo
				msgErro = "Atenção: Por favor, selecione um arquivo do tipo .xlsx";
			} else if (!nomeArquivo.contains(".xlsx")) {
				msgErro = "Atenção: Tipo de arquivo não permitido!";
			}

			tamanhoArquivo = file.getSize();

			if (tamanhoArquivo > tamanhoArquivoLimite) { // Verifica se o arquivo excede o tamanho limite
				msgErro = "Atenção: Tamanho máximo permitido do arquivo é 16 MB!";
			}
		} catch (Exception e) {
			throw new BusinessException("Não foi possível gerar o script: " + e);
		}
		return msgErro;
	}

	/**
	 * Metodo responsavel o upload do arquivo.
	 * 
	 * @throws IOException
	 * @throws BusinessException
	 * 
	 */
	@Override
	public Boolean confirmarUpload(MultipartFile file) throws BusinessException
	{
		Path path = null;
		try {
			byte[] bytes;
			bytes = file.getBytes();
			path = Paths.get(PASTA_ARQUIVO_IMPORTADO + file.getOriginalFilename());
			Files.write(path, bytes);
		} catch (IOException e) {
			throw new BusinessException("Não foi possível fazer o upload do arquivo: " + e);
		}
		return true;
	}

	/**
	 * Metodo responsavel por ler o arquivo xlsx e gerar os scripts.
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws BusinessException
	 * 
	 */
	@Override
	public Boolean confirmarGerecaoScript(String nomeArquivo, String ntabela) throws BusinessException 
	{
		String nomeArquivoConvertido = "";
		String login = "";
		String flag = "";

		Integer loginFinal = 0;
		Integer flagFinal = 0;

		Integer sheetIdx = 0; // 0 caso seja a primeira folha

		try {
			nomeArquivoConvertido = nomeArquivo.replace(".xlsx", "");
			
			if (ntabela.isEmpty()) {
				return false;
			}

			FileInputStream fileInStream = new FileInputStream(PASTA_ARQUIVO_IMPORTADO + nomeArquivo);

			XSSFWorkbook workBook = new XSSFWorkbook(fileInStream); // Caminho para o arquivo Excel
			XSSFSheet selSheet = workBook.getSheetAt(sheetIdx);

			Iterator<Row> rowIterator = selSheet.iterator(); // Percorre todas as linhas do arquivo Excel

			BufferedWriter StrUpdate = new BufferedWriter(new FileWriter(PASTA_SCRIPT_UPDATE + nomeArquivoConvertido + ".txt"));

			BufferedWriter StrRollBack = new BufferedWriter(new FileWriter(PASTA_SCRIPT_ROLLBACK + nomeArquivoConvertido + ".txt"));

			BufferedWriter StrValidacao = new BufferedWriter(new FileWriter(PASTA_SCRIPT_VALIDACAO + nomeArquivoConvertido + ".txt"));

			StrValidacao.append("SET SERVEROUTPUT ON;\r\n");
			StrValidacao.append("\r\n");
			StrValidacao.append("BEGIN\r\n");
			StrValidacao.append("execute immediate\r\n");

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() == 0) { // Pula a linha do titulo
					continue;
				}

				Iterator<Cell> cellIterator = row.cellIterator(); // Insere "," entre as colunas

				StringBuffer sb = new StringBuffer();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					if (sb.length() != 0) {
						sb.append(",");
					}

					switch (cell.getCellTypeEnum()) { // Se estiver usando o poi 4.0 ou mais, mudar para cell.getCellType
					case STRING:
						sb.append(cell.getStringCellValue());
						break;
					case NUMERIC:
						sb.append(cell.getNumericCellValue());
						break;
					case BOOLEAN:
						sb.append(cell.getBooleanCellValue());
						break;
					default:
					}
				}
				loginFinal = sb.indexOf(",");
				flagFinal = sb.length();

				if (sb.toString().isEmpty()) { // Encerra quando chegar na ultima linha
					break;
				}

				if (sb.length() != 0) {
					flag = sb.substring(flagFinal - 1, flagFinal);
					login = sb.substring(0, loginFinal);
				}
				// Bloco do script de update
				StrUpdate.append("UPDATE " + ntabela + " SET FG_LOGIN_ROLLOUT = '" + flag + "' WHERE USERID = '" + login + "';\r\n");

				// Blodo de script de rollback
				StrRollBack.append("UPDATE " + ntabela + " SET FG_LOGIN_ROLLOUT = 'N' WHERE USERID = '" + login + "';\r\n");

				// Bloco do script de validacao
				StrValidacao.append("	'begin ''" + ntabela + "'' set FG_LOGIN_ROLLOUT = ''S'' WHERE USERID = ''" + login + "'' AND FG_LOGIN_ROLLOUT = ''" + flag + "'';\r\n");
				StrValidacao.append("		if sql%rowcount >0 then\r\n");
				StrValidacao.append(" 			dbms_output.put_line(''SUCESSO: A matrícula: " + login + " foi atualizada corretamente com a flag: " + flag + ".'');\r\n");
				StrValidacao.append("		else\r\n");
				StrValidacao.append(" 			dbms_output.put_line(''ERRO: A matrícula: " + login + " não foi atualizada corretamente.'');\r\n");
				StrValidacao.append("		end if;\r\n");
				StrValidacao.append("		rollback;\r\n");
				StrValidacao.append("	end;';\r\n");
				StrValidacao.append("\r\n");

				StrUpdate.flush();
				StrRollBack.flush();
				StrValidacao.flush();
			}
			StrValidacao.append("END;");
			
			StrValidacao.close();
			StrRollBack.close();
			StrUpdate.close();
			workBook.close();
		} catch (IOException e) {
			throw new BusinessException("Não foi possível gerar os scripts: " + e);
		}
		return true;
	}

}
