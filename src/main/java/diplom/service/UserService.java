package diplom.service;

import diplom.model.entity.user.User;
import diplom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean addUser(User user) {
        boolean update = user != null && !userRepository.existsByEmail(user.getEmail());

        if (update) {
            userRepository.save(user);
        }

        return update;
    }

    public boolean deleteUser(Long id) {
        var user = userRepository.findById(id);
        boolean update = user.isPresent();

        if (update) {
            userRepository.deleteById(id);
        }

        return update;
    }

    public boolean updateUserData(User user) {
        var update = user != null;

        if (update) {
            var temp = userRepository.findById(user.getId());

            if (temp.isPresent()) {
                User existingUser = temp.get();
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());
                existingUser.setEmail(user.getEmail());
                existingUser.setPhoneNumber(user.getPhoneNumber());
                existingUser.setPassword(user.getPassword());
                existingUser.setAddress(user.getAddress());  // Обновляем все поля, которые пришли в запросе

                userRepository.save(existingUser);  // Сохраняем обновленного пользователя
            }
        }

        return update;
    }


    public User getUser(long id) {
        var user = userRepository.findById(id);
        var update = user.isPresent();

        return update ? user.get() : null;
    }
}
