package main.com.julio.repository;

import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.service.LoggingService;
import main.com.julio.util.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Prospect> findAll() {
        return prospects.stream()
                .sorted((p1, p2) ->
                        p1.getRaisonSociale().compareToIgnoreCase(p2.getRaisonSociale()))
                .collect(Collectors.toList());
    }

    private void initialiserDonneesDemo() {
        Adresse adresse1 = new Adresse("10", "Metz", "54390", "Frouard");
        Adresse adresse2 = new Adresse("101", "De La Resistance", "54390", "Frouard");

        prospects.add(
                new Prospect(
                        "Boulangerie", adresse1,
                        "0696589632",
                        "boulangerie@boulangerie.fr",
                        "",
                        DateUtils.parseDate("10/01/2021"),
                        Interesse.OUI
                )
        );
        prospects.add(
                new Prospect(
                        "Supermarché", adresse2,
                        "0123456789",
                        "supermarche@supermarche.fr",
                        "",
                        DateUtils.parseDate("12/01/2024"),
                        Interesse.OUI
                )
        );

    }

}
