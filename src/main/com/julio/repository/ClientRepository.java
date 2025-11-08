package main.com.julio.repository;

import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.service.LoggingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientRepository {
    public List<Client> clients;

    public ClientRepository() {
        this.clients = new ArrayList<>();
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
        LoggingService.log("Client ajouté: ID= " + client.getId());
    }

    public boolean update(Client client) {
        //================== PROBAR ESTE PROCESO ++++++++++++++++++++++
//        for (Client c : this.clients) {
//            if (c.getId() == client.getId()) {
//                this.clients.remove(c);
//                this.clients.add(client);
//                return true;
//            }
//        }

        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == client.getId()) {
                this.clients.set(i, client);
                LoggingService.log("Client mis à jour: ID= " + client.getId());
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        boolean removed = clients.removeIf(client -> client.getId() == id);
        if (removed) {
            LoggingService.log("Client supprimé: ID= " + id);
        }
        return removed;
    }

    public Client findById(int id) {
        return clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Client> findAll() {
        return clients.stream()
                .sorted((c1, c2) -> c1.getRaisonSociale().compareToIgnoreCase(c2.getRaisonSociale()))
                .collect(Collectors.toList());
    }

    public void initialiserDonneesDemo() {
        Adresse adresse1 = new Adresse("10", "Victor Hugo", "54000", "Nancy");
        Adresse adresse2 = new Adresse("102", "Victor Duquesnay", "97233", "Schoelcher");
        Adresse adresse3 = new Adresse("25", "L'Esperance", "54390", "Frouard");
//        Contrat contrat1 = new Contrat("1","Mega", 50000);
        new Client(
                "IBM",
                adresse1,
                "0778663083",
                "ibm@ibm.com",
                "",
                5000,
                10
        );

        new Client(
                "Apple",
                adresse2,
                "155466284",
                "apple@apple.com",
                "",
                50000,
                100
        );

        new Client(
                "Microsoft",
                adresse3,
                "987456213",
                "microsoft@microsoft.com",
                "",
                500000,
                1000
        );


    }

}
