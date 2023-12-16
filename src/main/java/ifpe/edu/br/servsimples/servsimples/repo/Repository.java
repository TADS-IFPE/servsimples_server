package ifpe.edu.br.servsimples.servsimples.repo;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.model.Service;
import ifpe.edu.br.servsimples.servsimples.model.User;

public class Repository {
    private String TAG = Repository.class.getSimpleName();
    private static Repository instance;
    private final UserRepo userRepo;
    private final ServiceRepo serviceRepo;

    private Repository(UserRepo userRepo, ServiceRepo serviceRepo) {
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
    }

    public static Repository create(UserRepo userRepo, ServiceRepo serviceRepo) {
        if (instance == null) {
            instance = new Repository(userRepo, serviceRepo);
        }
        return instance;
    }

    public static Repository getInstance() {
        return instance;
    }

    private static void setInstance() {
    }

    public User getUserByCPF(String cpf) {
        return userRepo.findByCpf(cpf);
    }

    public void removeUser(User restoredUser) {
        userRepo.delete(restoredUser);
    }

    public void updateUser(User restoredUser) {
        saveUser(restoredUser);
    }

    public void saveUser(User user) {
        try {
            userRepo.save(user);
        } catch (Exception e) {
            ServSimplesApplication.logi(TAG, "save user fail: "
                    + e.getMessage());
        }
    }

    public User getUserByUsername(String userName) {
        return userRepo.findByUserName(userName);
    }

    public User getUserByService(Service service) {
        return userRepo.findUsersByServicesContaining(service);
    }
}