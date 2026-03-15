package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.person.user.dto.UserDTO;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/user")
@Tag(name = "User", description = "Esta sección es dedicada a las operaciones relacionadas con los Usuarios")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping("/create-user")
    public UserModel crearUsuario(@RequestBody UserDTO userdto){
        return userService.saveUser(userdto);
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserModel updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserModel>> getAllUsers() {
        List<UserModel> users = userService.getAllUser();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all-user/enterprise/{enterpriseId}")
    public ResponseEntity<List<UserModel>> getUsersByEnterprise(@PathVariable Long enterpriseId) {
        List<UserModel> users = userService.getUsersByEnterprise(enterpriseId);
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 si no hay usuarios
        }
        return ResponseEntity.ok(users);
    }
}
