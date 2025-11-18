package main.com.julio.repository;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientRepository {
    public List<Client> clients;

    public ClientRepository() throws ValidationException {
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
        clients.add(new Client(
                "IBM",
                adresse1,
                "0778663083",
                "ibm@ibm.com",
                "",
                5000,
                10
        ));

        clients.add(new Client(
                "Apple",
                adresse2,
                "0778663083",
                "apple@apple.com",
                "",
                50000,
                100
        ));
        clients.add(new Client(
                "Microsoft",
                adresse3,
                "0778663083",
                "microsoft@microsoft.com",
                "",
                500000,
                1000
        ));
    }
}
