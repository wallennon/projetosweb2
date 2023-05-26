package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.UserDto;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.models.User;
import br.edu.unifip.ecommerceapi.services.UserService;
import br.edu.unifip.ecommerceapi.utils.FileDownloadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    final UserService userService;

    public UserController(UserService userService){this.userService = userService;}

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getUsersIsActive(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") UUID id){
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> saveUser(@Valid UserDto userDto, HttpServletRequest request) throws IOException {
        var user = new User();

        BeanUtils.copyProperties(userDto, user);

//        UUID categoryId = null;
//
//        if (userDto.getCategory() != null){
//            categoryId = userDto.getCategory();
//        }

        MultipartHttpServletRequest multiparRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multiparRequest.getFile("image");

        if (multipartFile!= null){
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                user.setImage("/api/user-image/" + filecode);
            }catch(IOException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image no accepted.");
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") UUID id){
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        userService.delete(userOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id, HttpServletRequest request){
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        Map<Object, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()){
            objectMap.put(entry.getKey(), entry.getValue()[0]);
        }

        // Salvar a url da imagem em uma variável separada
        String imageUrl = null;
        MultipartHttpServletRequest multipartRquest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRquest.getFile("image");
        if (multipartFile != null){
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-images/";

            try{
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                imageUrl = "/api/users/user-images/" + filecode;
            } catch(IOException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted");
            }

            // Adicionar a url da imagem ao objeto mapeado, se ela foi enviada

            if(imageUrl != null){
                objectMap.put("image", imageUrl);
            }

            userService.partialUpdate(userOptional.get(), objectMap);

        }
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());

    }

    @GetMapping("/user-images/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode")String fileCode) {
        FileDownloadUtil downloadUtil = new FileDownloadUtil();

        Resource resource = null;

        try {
            resource = downloadUtil.getFileAsResource(fileCode, "user-images");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        MediaType contentType;

        if (Objects.equals(FilenameUtils.getExtension(resource.getFilename()), "jpg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else {
            contentType = MediaType.IMAGE_PNG;
        }

        String headerValue = "Attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}


