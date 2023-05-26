package br.edu.unifip.ecommerceapi.services;

import br.edu.unifip.ecommerceapi.models.User;
import br.edu.unifip.ecommerceapi.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    final UserRepository userRepository;

    public UserService(UserRepository userRepository){this.userRepository = userRepository;}

    public List<User> findAll(){return userRepository.findAll();}

    public Optional<User> findById(UUID id) { return userRepository.findById(id);}

    @Transactional
    public User save(User user) { return userRepository.save(user);}

    @Transactional
    public void delete (User user) { userRepository.delete(user);}

    public List<User> findByActiveTrue(){ return userRepository.findByActiveTrue();}


    public User partialUpdate(User user, Map<Object, Object> objectMap) {
        objectMap.forEach((key,value) -> {
            Field field = ReflectionUtils.findField(User.class, (String) key);
            field.setAccessible(true);
        });
        return userRepository.save(user);
    }
    public List<User> findByName(String name){return userRepository.findByName(name);}
    public List<User> findByCategoryName(String name){return userRepository.findByCategoryName(name);}


}
