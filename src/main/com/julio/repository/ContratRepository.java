package main.com.julio.repository;

import main.com.julio.model.Contrat;
import main.com.julio.service.LoggingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContratRepository {
    private List<Contrat> contrats;

    public ContratRepository(){
        this.contrats = new ArrayList<>();
    }

    public void add(Contrat contrat) {
        this.contrats.add(contrat);
        LoggingService.log("Contrat ajouté: ID=" + contrat.getId());
    }

    public boolean update(Contrat contrat) {
        for (int i = 0; i < this.contrats.size(); i++) {
            if (this.contrats.get(i).getId() == contrat.getId()) {
                this.contrats.set(i, contrat);
                LoggingService.log("Contrat mis à jour: ID=" + contrat.getId());
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        boolean removed = contrats.removeIf(contrat -> contrat.getId() == id);
        if (removed) {
            LoggingService.log("Contrat supprimé: ID=" + id);
        }
        return removed;
    }

    public Contrat findById(int id) {
        return contrats.stream()
                .filter(contrat -> contrat.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Contrat> findByClientId(int clientId) {
        return contrats.stream()
                .filter(contrat -> contrat.getClientId() == clientId)
                .collect(Collectors.toList());
    }

    public List<Contrat> findAll() {
        return new ArrayList<>(contrats);
    }
}
