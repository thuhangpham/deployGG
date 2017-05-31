package bootsample.controller;

import org.apache.catalina.connector.Request;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bootsample.dao.PostRepository;
import bootsample.model.Post;
import bootsample.service.DriveSample;
import bootsample.service.StorageFileNotFoundException;
import bootsample.service.StorageProperties;
import bootsample.service.StorageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

@Controller
public class FileUploadController {

	@Autowired
	private PostRepository postRepository;

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {
		Post post = new Post();
		model.addAttribute("post", post);
		return "index";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
/*
	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
			throws IllegalStateException, IOException {

		File convFile = storageService.store(file);

		UploadService service = new UploadService();
		String url = service.Upload(convFile);

		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "! And Uploaded to Drive View: " + url);
		return "redirect:/";
	}*/

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/{id}")
	public String findById(@PathVariable("id")Integer id, Model model) {
		Post post = postRepository.findOne(id);
		model.addAttribute("post", post);
		return "index";
	}
	
	@RequestMapping(method = RequestMethod.POST, value={"/"})
	public String insert(ModelMap model,
			@ModelAttribute("post")Post post,@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
					throws Exception {
		DriveSample upload = new DriveSample();
		String filePath = storageService.store(file);
		String path = System.getProperty("user.dir")+"/upload-dir/" + filePath;
		/*File f = new File(path);*/
		String url = upload.excuteUploadFile(path);
		post.setUrl(url);
		postRepository.save(post);
		return "redirect:/";
	}
	
	@ModelAttribute("posts")
	public List<Post> getDocuments(){
		return (List<Post>) postRepository.findAll();
	}
}
