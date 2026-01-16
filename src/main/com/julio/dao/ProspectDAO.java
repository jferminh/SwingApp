package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.model.Prospect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProspectDAO extends SocieteDAO{
    public ProspectDAO() throws SQLException, DAOException {
        super();
    }

//    public List<Prospect> findAll() throws SQLException {
//        List<Prospect> prospects = new ArrayList<>();
//        String query = "SELECT s.id_societe, s.raison_sociale, s.adresse_id, s.telephone, " +
//                "s.email, s.commentaires, " +
//                "p.id_prospect, p.date"
//    }
}
