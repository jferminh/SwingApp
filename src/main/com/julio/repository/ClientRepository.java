package main.com.julio.repository;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientRepository {
    private List<Client> clients;
    private ContratRepository contratRepo;

    public ClientRepository(ContratRepository contratRepo) throws ValidationException {
        this.clients = new ArrayList<>();
        this.contratRepo = contratRepo;
        initialiserDonneesDemo();
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public void add(Client client) {
        this.clients.add(client);
    }

    public boolean update(Client client) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == client.getId()) {
                this.clients.set(i, client);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {

        return clients.removeIf(client -> client.getId() == id);
    }

    public Client findById(int id) {
        return clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Client> findAll() {
        return clients.stream()
                .sorted((c1, c2) ->
                        c1.getRaisonSociale().compareToIgnoreCase(c2.getRaisonSociale()))
                .collect(Collectors.toList());
    }

    public void initialiserDonneesDemo() throws ValidationException {
        Adresse adresse1 = new Adresse(
                "10",
                "Victor Hugo",
                "54000",
                "Nancy"
        );
        Adresse adresse2 = new Adresse(
                "102",
                "Victor Duquesnay",
                "97233",
                "Schoelcher"
        );
        Adresse adresse3 = new Adresse(
                "25",
                "L'Esperance",
                "54390",
                "Frouard"
        );
        Client c1 = new Client(
                "IBM",
                adresse1,
                "0778663083",
                "ibm@ibm.com",
                "",
                5000,
                10
        );
        add(c1);

        Client c2 = new Client(
                "Apple",
                adresse2,
                "0778663083",
                "apple@apple.com",
                "",
                50000,
                100
        );
        add(c2);

        Client c3 = new Client(
                "Microsoft",
                adresse3,
                "0778663083",
                "microsoft@microsoft.com",
                "",
                500000,
                1000
        );
        add(c3);

        prechargerContrats(c1.getId(),
                new Contrat(c1.getId(), "TMA ERP", 45000),
                new Contrat(c1.getId(), "Projet BI 2025", 82000)
        );

        prechargerContrats(c2.getId(),
                new Contrat(c2.getId(), "Migration Cloud", 150000),
                new Contrat(c2.getId(), "Support Niveau 2", 36000),
                new Contrat(c2.getId(), "Audit Cybersécurité", 22000)
        );

        prechargerContrats(c3.getId(),
                new Contrat(c3.getId(), "Refonte Site Web", 28000)
        );


    }

    private void prechargerContrats(int clientId, Contrat... contrats ) {
        for (Contrat ct : contrats) {
            contratRepo.add(ct);
            Client cli = findById(clientId);
            if (ct != null) {
                cli.ajouterContrat(ct);
            }
        }
    }
}
