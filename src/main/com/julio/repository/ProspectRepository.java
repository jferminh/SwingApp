package main.com.julio.repository;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.util.DateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repository gérant la persistance et l'accès aux données des prospects.
 * <p>
 * Cette classe implémente le pattern Repository pour centraliser toutes
 * les opérations CRUD (Create, Read, Update, Delete) sur les prospects.
 * Elle maintient une collection en mémoire de tous les prospects et fournit
 * un tri standardisé par raison sociale via un {@link Comparator} dédié.
 * </p>
 * <p>
 * Le repository initialise automatiquement des données de démonstration
 * lors de sa création pour faciliter les tests et la démonstration de
 * l'application.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Prospect
 * @see Interesse
 */
public class ProspectRepository {

    /** Collection en mémoire contenant tous les prospects */
    private final List<Prospect> prospects;

    /**
     * Comparateur statique pour trier les prospects par raison sociale.
     * <p>
     * Utilise {@link Optional#ofNullable} pour gérer les raisons sociales nulles
     * en les traitant comme des chaînes vides, garantissant ainsi un tri stable
     * sans risque de {@link NullPointerException}.
     * </p>
     */
    public static final Comparator<Prospect> BY_RAISON_SOCIALE =
            Comparator.comparing((Prospect p) -> Optional.ofNullable(p.getRaisonSociale()).orElse(""));

    /**
     * Constructeur initialisant le repository avec des données de démonstration.
     * <p>
     * Crée un repository vide puis le peuple automatiquement avec 2 prospects
     * de démonstration (Boulangerie, Supermarché). Cette initialisation facilite
     * les tests et la démonstration de l'application.
     * </p>
     *
     * @throws ValidationException si les données de démonstration ne respectent pas les règles de validation
     */
    public ProspectRepository() throws ValidationException {
        this.prospects = new ArrayList<>();
        initialiserDonneesDemo();
    }

    /**
     * Ajoute un nouveau prospect au repository.
     * <p>
     * Le prospect est ajouté à la fin de la collection.
     * </p>
     *
     * @param prospect le prospect à ajouter (ne devrait pas être null)
     */
    public void add(Prospect prospect) {
        this.prospects.add(prospect);
    }

    /**
     * Met à jour un prospect existant dans le repository.
     * <p>
     * Recherche le prospect par son identifiant et remplace l'ancienne instance
     * par la nouvelle.
     * </p>
     *
     * @param prospect le prospect avec les nouvelles données (doit avoir un ID valide)
     */
    public void update(Prospect prospect) {
        for (int i = 0; i < this.prospects.size(); i++) {
            if (this.prospects.get(i).getId() == prospect.getId()) {
                this.prospects.set(i, prospect);
                return;
            }
        }
    }

    /**
     * Supprime un prospect du repository par son identifiant.
     * <p>
     * Utilise {@link List#removeIf} pour supprimer le prospect correspondant.
     * </p>
     *
     * @param id identifiant du prospect à supprimer
     * @return true si un prospect a été supprimé, false si aucun prospect ne correspond
     */
    public boolean delete(int id) {
        return prospects.removeIf(prospect -> prospect.getId() == id);
    }

    /**
     * Recherche un prospect par son identifiant.
     * <p>
     * Utilise les Streams Java 8 pour rechercher efficacement le prospect.
     * </p>
     *
     * @param id identifiant du prospect recherché
     * @return le prospect trouvé ou null si aucun prospect ne correspond
     */
    public Prospect findById(int id) {
        return prospects.stream()
                .filter(prospect -> prospect.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne tous les prospects triés alphabétiquement par raison sociale.
     * <p>
     * Crée une copie défensive de la liste pour éviter les modifications non
     * contrôlées, puis applique le tri via le comparateur {@link #BY_RAISON_SOCIALE}.
     * Les valeurs nulles sont gérées en les traitant comme des chaînes vides.
     * </p>
     *
     * @return liste de tous les prospects triés par raison sociale (A-Z)
     */
    public List<Prospect> findAll() {
        List<Prospect> copy = new ArrayList<>(prospects);
        copy.sort(BY_RAISON_SOCIALE);
        return copy;
    }

    /**
     * Initialise le repository avec des données de démonstration.
     * <p>
     * Crée 2 prospects fictifs avec leurs adresses :
     * <ul>
     *   <li><b>Boulangerie</b> (Frouard) - Prospecté le 10/01/2021, intéressé</li>
     *   <li><b>Supermarché</b> (Frouard) - Prospecté le 12/01/2024, intéressé</li>
     * </ul>
     * Cette méthode est appelée automatiquement par le constructeur.
     * Les dates sont parsées via {@link DateUtils#parseDate(String)}.
     * </p>
     *
     * @throws ValidationException si les données de démonstration violent les règles métier
     */
    private void initialiserDonneesDemo() throws ValidationException {
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
