package main.com.julio.model;

import main.com.julio.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class Client extends Societe {
    private static int compteurId = 1;
    private long chiffreAffaires;
    private int nbEmployes;
    private List<Contrat> contrats;

    public Client(String raisonSociale, Adresse adresse, String telephone,
                  String email, String commentaires, long chiffreAffaires,
                  int nbEmployes) throws ValidationException {
        super(compteurId++, raisonSociale, adresse, telephone, email, commentaires);
        setChiffreAffaires(chiffreAffaires);
        setNbEmployes(nbEmployes);
        this.contrats = new ArrayList<>();
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

    public void setChiffreAffaires(long chiffreAffaires) throws ValidationException {
        if (chiffreAffaires < 200) {
            throw new ValidationException("Le chiffre d'affaires doit être >= 200.");
        }
        this.chiffreAffaires = chiffreAffaires;
    }

    public void setNbEmployes(int nbEmployes) throws ValidationException {
        if (nbEmployes < 1) {
            throw new ValidationException("Le nombre d'employés doit être >= 1");
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
    }

    public void supprimerContrat(Contrat contrat) {
        contrats.remove(contrat);

    }

    public static void resetCompteur(){
        compteurId = 1;
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
