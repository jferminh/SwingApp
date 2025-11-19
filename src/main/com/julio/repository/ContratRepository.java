package main.com.julio.repository;

import main.com.julio.model.Contrat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContratRepository {
    private List<Contrat> contrats;

    public ContratRepository() {
        this.contrats = new ArrayList<>();
    }

    public void add(Contrat contrat) {
        this.contrats.add(contrat);
    }

    public void update(Contrat contrat) {
        for (int i = 0; i < this.contrats.size(); i++) {
            if (this.contrats.get(i).getId() == contrat.getId()) {
                this.contrats.set(i, contrat);
                return;
            }
        }
    }

    public boolean delete(int id) {

        return contrats.removeIf(contrat -> contrat.getId() == id);
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
