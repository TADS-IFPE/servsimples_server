package ifpe.edu.br.servsimples.servsimples.repo;

import ifpe.edu.br.servsimples.servsimples.model.User;

public interface Subscribeable {

    void subscribe(User user);

    void unsubscribe(User user);

    void notifySubscribers();
}