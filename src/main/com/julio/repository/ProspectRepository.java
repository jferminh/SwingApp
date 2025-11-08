package main.com.julio.repository;

import main.com.julio.model.Prospect;
import main.com.julio.service.LoggingService;

import java.util.ArrayList;
import java.util.List;

public class ProspectRepository {
    private List<Prospect> prospects;

    public ProspectRepository() {
        this.prospects = new ArrayList<>();
        initialiserDonneesDemo();
    }

    public void add(Prospect prospect) {
        this.prospects.add(prospect);
        LoggingService.log("Prospect ajouté: ID= " + prospect.getId());
    }

    public boolean update(Prospect prospect) {
        for (int i = 0; i < this.prospects.size(); i++) {
            if (this.prospects.get(i).getId() == prospect.getId()) {
                this.prospects.set(i, prospect);
                LoggingService.log("Prospect mis à jour: ID=" + prospect.getId());
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        boolean removed = prospects.removeIf(prospect -> prospect.getId() == id);
        if (removed) {
            LoggingService.log("Prospect supprimé: ID= " + id);
        }
        return removed;
    }

    public Prospect findById(int id) {
        return prospects.stream()
                .filter(prospect -> prospect.getId() == id)
                .findFirst()
                .orElse(null);
    }
    private void initialiserDonneesDemo() {
    }
}
