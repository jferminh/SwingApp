package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.LoggingService;

import java.util.ArrayList;
import java.util.List;

public class Client extends Societe {
    private long chiffreAffaires;
    private int nbEmployes;
    private List<Contrat> contrats;

    public Client(String raisonSociale, Adresse adresse, String telephone,
                  String email, String commentaires, long chiffreAffaires,
                  int nbEmployes) {
        super(raisonSociale, adresse, telephone, email, commentaires);
        setChiffreAffaires(chiffreAffaires);
        setNbEmployes(nbEmployes);
        this.contrats = new ArrayList<>();
        LoggingService.log("Client " + this + " créé");
    }

    public long getChiffreAffaires() {
        return chiffreAffaires;
    }

    public int getNbEmployes() {
        return nbEmployes;
    }

    public List<Contrat> getContrats() {
        return new ArrayList<>(contrats);
    }

    public void setChiffreAffaires(long chiffreAffaires) {
        if (chiffreAffaires < 200) {
            throw new ValidationException("chiffreAffaires", "Le chiffre d'affaires doit être >= 200.");
        }
        this.chiffreAffaires = chiffreAffaires;
    }

    public void setNbEmployes(int nbEmployes) {
        if (nbEmployes < 1) {
            throw new ValidationException("nbEmployes", "Le nombre d'employés doit être >= 1");
        }
        this.nbEmployes = nbEmployes;
    }

    public void setContrats(List<Contrat> contrats) {
        this.contrats = contrats;
    }

    public void ajouterContrat(Contrat contrat) {
        if (contrat != null && !contrats.contains(contrat)) {
            contrats.add(contrat);
        }
        LoggingService.log("Contrat ajouté au client ID :" + getId());
    }

    public void supprimerContrat(Contrat contrat) {
        if (contrats.remove(contrat)) {
            LoggingService.log("Contrat supprimé de client ID : " + getId());
        }
    }



    @Override
    public String toString() {
        return getRaisonSociale() + " (Client)";
    }

    @Override
    public String getTypeSociete() {
        return "Client";
    }
}
