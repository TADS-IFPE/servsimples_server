package ifpe.edu.br.servsimples.servsimples.repo;

import ifpe.edu.br.servsimples.servsimples.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByCpf(String CPF);
    User findByUserName(String Username);
}