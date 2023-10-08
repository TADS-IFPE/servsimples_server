package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.model.Cost;
import ifpe.edu.br.servsimples.servsimples.model.Service;

import java.util.List;

public class ServiceManager {
    public static final int SERVICE_VALID = 100;
    public static final int SERVICE_IS_NULL = 101;
    public static final int SERVICE_NAME_ERROR = 102;
    public static final int SERVICE_COST_IS_NULL = 103;
    public static final int SERVICE_COST_ERROR = 104;
    public static final int SERVICE_VALUE_ERROR = 105;
    public static final int SERVICE_IS_EMPTY = 106;
    public static final int SERVICE_DUPLICATE = 107;

    public int getServiceValidationCode(Service service) {
        if (service == null) return SERVICE_IS_NULL;
        String name = service.getName();
        if (name == null || name.isEmpty() || name.isBlank()) return SERVICE_NAME_ERROR;
        Cost cost = service.getCost();
        if (cost == null) return SERVICE_COST_IS_NULL;
        if (cost.getTime() == null || cost.getTime().isBlank() || cost.getTime().isEmpty()) return SERVICE_COST_ERROR;
        if (cost.getValue() == null || cost.getValue().isEmpty() || cost.getValue().isBlank())
            return SERVICE_VALUE_ERROR;
        return SERVICE_VALID;
    }

    public int getServiceValidationCode(List<Service> services) {
        if (services.isEmpty()) return SERVICE_IS_EMPTY;
        if (services.size() > 1) return SERVICE_DUPLICATE;
        return getServiceValidationCode(services.get(0));
    }
}