package test.com.julio.util;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;

import java.time.LocalDate;

public class TestDataBuilder {
    public static Adresse createAdresseSansId() throws ValidationException {
        return new Adresse("12", "Rue de Test", "75001", "Paris");
    }

    public static Adresse createAdresseAvecId(Integer id) throws ValidationException {
        Adresse adresse = new Adresse("12", "Rue de Test", "75001", "Paris");
        adresse.setId(id);
        return adresse;
    }

    public static Client createClientSansId() throws ValidationException {
        Adresse adresse = createAdresseSansId();
        return new Client(
                "Entreprise Test SARL",
                adresse,
                "0123456789",
                "test@entreprise.fr",
                "Client de test",
                500000,
                25
        );
    }
    public static Client createClientAvecId(Integer id, Integer adresseId) throws ValidationException {
        Adresse adresse = createAdresseAvecId(adresseId);
        Client client = new Client(
                "Entreprise Test SARL",
                adresse,
                "0123456789",
                "test@entreprise.fr",
                "Client de test",
                500000,
                25
        );
        client.setId(id);
        return client;
    }

    public static Prospect createProspectSansId() throws ValidationException {
        Adresse adresse = createAdresseSansId();
        return new Prospect(
                "Prospect Test SA",
                adresse,
                "0123456789",
                "contact@prospect.fr",
                "Prospect intéresé",
                LocalDate.of(2026,1,10),
                Interesse.OUI
        );
    }

    public static Prospect createProspectAvecId(Integer id, Integer adresseId) throws ValidationException {
        Adresse adresse = createAdresseAvecId(adresseId);
        Prospect prospect = new Prospect(
                "Prospect Test SA",
                adresse,
                "0456789123",
                "contact@prospect.fr",
                "Prospect intéressé",
                LocalDate.of(2026, 1, 10),
                Interesse.OUI
        );
        prospect.setId(id);
        return prospect;
    }
}
