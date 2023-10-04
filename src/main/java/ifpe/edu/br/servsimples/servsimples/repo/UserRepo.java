package ifpe.edu.br.servsimples.servsimples.repo;

import ifpe.edu.br.servsimples.servsimples.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {

    User findByCpf(String cpf);
}