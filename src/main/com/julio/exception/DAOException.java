package main.com.julio.exception;

/**
 * Exception de base pour toutes les erreurs liées aux opérations DAO.
 * Permet de distinguer les erreurs DAO des autres exceptions de l'application.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public class DAOException extends Exception {

    private final ErrorCode errorCode;
    private final String operation;
    private final Object entityId;

    /**
     * Constructeur avec message simple.
     */
    public DAOException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;

        this.operation = null;
        this.entityId = null;
    }

    /**
     * Constructeur avec message et cause.
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
        this.operation = null;
        this.entityId = null;
    }

    /**
     * Constructeur complet avec code d'erreur, opération et ID de l'entité.
     */
    public DAOException(ErrorCode errorCode, String operation, Object entityId, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.entityId = entityId;
    }

    /**
     * Constructeur complet sans cause.
     */
    public DAOException(ErrorCode errorCode, String operation, Object entityId, String message) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.entityId = entityId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getOperation() {
        return operation;
    }

    public Object getEntityId() {
        return entityId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DAOException{");
        sb.append("errorCode=").append(errorCode);
        if (operation != null) {
            sb.append(", operation='").append(operation).append('\'');
        }
        if (entityId != null) {
            sb.append(", entityId=").append(entityId);
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Codes d'erreur pour catégoriser les erreurs DAO.
     */
    public enum ErrorCode {
        // Erreurs de connexion
        CONNECTION_ERROR("Erreur de connexion à la base de données"),

        // Erreurs CRUD
        CREATE_ERROR("Erreur lors de la création"),
        READ_ERROR("Erreur lors de la lecture"),
        UPDATE_ERROR("Erreur lors de la mise à jour"),
        DELETE_ERROR("Erreur lors de la suppression"),

        // Erreurs de contraintes
        FOREIGN_KEY_VIOLATION("Violation de contrainte de clé étrangère"),
        UNIQUE_CONSTRAINT_VIOLATION("Violation de contrainte d'unicité"),
        NOT_NULL_VIOLATION("Violation de contrainte NOT NULL"),
        CHECK_CONSTRAINT_VIOLATION("Violation de contrainte CHECK"),

        // Erreurs métier
        ENTITY_NOT_FOUND("Entité introuvable"),
        INVALID_PARAMETER("Paramètre invalide"),
//        NOT_FOUND("Aucune enregistrement trouvée"),

        // Erreurs générales
        TRANSACTION_ERROR("Erreur de transaction"),
        GENERAL_ERROR("Erreur générale");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
