package diplom.service;

import diplom.entity.account.Account;
import diplom.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    public final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean addUser(Account account) {
        boolean update = account != null && !accountRepository.isExistsByEmail(account.getEmail());

        if (update) {
            accountRepository.save(account);
        }

        return update;
    }

    public boolean deleteUser(UUID id) {
        var user = accountRepository.findById(id);
        boolean update = user.isPresent();

        if (update) {
            accountRepository.deleteById(id);
        }

        return update;
    }

    public boolean updateUserData(Account account) {
        var update = account != null;

        if (update) {
            var temp = accountRepository.findById(account.getId());

            if (temp.isPresent()) {
                Account existingAccount = temp.get();
                existingAccount.setFirstName(account.getFirstName());
                existingAccount.setLastName(account.getLastName());
                existingAccount.setEmail(account.getEmail());
                existingAccount.setPhoneNumber(account.getPhoneNumber());
                existingAccount.setPassword(account.getPassword());
                existingAccount.setAddress(account.getAddress());  // Обновляем все поля, которые пришли в запросе

                accountRepository.save(existingAccount);  // Сохраняем обновленного пользователя
            }
        }

        return update;
    }


    public Account getUser(UUID id) {
        var user = accountRepository.findById(id);
        var update = user.isPresent();

        return update ? user.get() : null;
    }
}
