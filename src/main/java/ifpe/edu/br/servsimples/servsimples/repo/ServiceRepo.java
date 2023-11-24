package ifpe.edu.br.servsimples.servsimples.repo;

import ifpe.edu.br.servsimples.servsimples.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepo extends JpaRepository<Service, Long> {

    List<Service> findAllByCategory(String category);
}
