import controlleur.QueryController;
import java.util.Map;
import javax.swing.UIManager;
import model.Attribut;
import model.Domaine;
import model.Relation;
import ui.MainView;
import utils.DataPersistence;

public class Main {
    public static void main(String[] args) {
        setNimbusLookAndFeel();
        
        MainView frame = new MainView();
        QueryController controller = new QueryController(frame);
        
        loadDemoData(controller);
        
        frame.setVisible(true);
    }
    
    private static void setNimbusLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Nimbus LookAndFeel not available, using default");
        }
    }
    
    private static void loadDemoData(QueryController controller) {
        DataPersistence persistence = new DataPersistence();
        
        Map<String, Relation> tables = persistence.loadAllTables();
        
        if (!tables.isEmpty()) {
            for (Map.Entry<String, Relation> entry : tables.entrySet()) {
                controller.addRelation(entry.getKey(), entry.getValue());
            }
            System.out.println("Données chargées: " + tables.size() + " table(s)");
        } else {
            System.err.println("Aucune table trouvée dans le répertoire data/");
        }
    }

    private static void demo() {

        Attribut attr1 = new Attribut("Age",
                new Domaine("Age", Integer.class, 0, 120, null));

        Attribut attr2 = new Attribut("Name",
                new Domaine("Name", String.class, null, null,
                        new Object[] { "Alice", "Bob", "Charlie", "David", "Emma" }));

        Attribut attr3 = new Attribut("Salaire",
                new Domaine("Salaire", Double.class, 0.0, 1000000.0, null));

        Attribut attr4 = new Attribut("Note",
                new Domaine("Note", Double.class, 0.0, 20.0, null));

        Attribut attr5 = new Attribut("Status",
                new Domaine("Status", String.class, null, null,
                        new Object[] { "ACTIF", "INACTIF", "SUSPENDU" }));

        Attribut[] attributs = { attr1, attr2, attr3, attr4, attr5 };
        Relation relation = new Relation("Employes", attributs);
        Relation relation2 = new Relation("Employes 2", attributs);

        System.out.println("=== Ajout de tuples dans Relation 1 ===\n");

        try {
            relation.ajouterTuple(new Object[] { 0, "Alice", 0.0, 0.0, "ACTIF" });
            relation.ajouterTuple(new Object[] { 120, "Bob", 1000000.0, 20.0, "INACTIF" });
            relation.ajouterTuple(new Object[] { 45, "Charlie", 45000.5, 10.0, "SUSPENDU" });
            relation.ajouterTuple(new Object[] { 32, "David", 80000.0, 15.5, "ACTIF" });
            relation.ajouterTuple(new Object[] { 25, "Emma", 35000.0, 12.0, "INACTIF" });

            // Tests de domaine invalides :
            // relation.ajouterTuple(new Object[] { -5, "Alice", 50000.0, 10.0, "ACTIF" });
            // // âge invalide
            // relation.ajouterTuple(new Object[] { 30, "Bob", 2000000.0, 10.0, "ACTIF" });
            // // salaire > max
            // relation.ajouterTuple(new Object[] { 40, "Charlie", 50000.0, 25.0, "ACTIF"
            // }); // note > 20
            // relation.ajouterTuple(new Object[] { 30, "Inconnu", 10000.0, 15.0, "ACTIF"
            // }); // nom invalide
            // relation.ajouterTuple(new Object[] { 30, "Alice", 10000.0, 15.0, "PAUSE" });
            // // status invalide

        } catch (IllegalArgumentException e) {
            System.err.println("Erreur (Relation 1): " + e.getMessage());
        }

        relation.afficherRelation();

        System.out.println("\n=== Ajout de tuples dans Relation 2 ===\n");

        try {
            relation2.ajouterTuple(new Object[] { 18, "Emma", 10000.0, 8.0, "ACTIF" });
            relation2.ajouterTuple(new Object[] { 55, "Bob", 500000.0, 19.0, "SUSPENDU" });
            relation2.ajouterTuple(new Object[] { 70, "Charlie", 750000.0, 17.5, "INACTIF" });

            // // Tests de domaine invalides
            // relation2.ajouterTuple(new Object[] { 150, "David", 100000.0, 10.0, "ACTIF"
            // }); // âge > 120
            // // relation2.ajouterTuple(new Object[] { 25, "Emma", "ErreurType", 10.0,
            // "ACTIF" }); // mauvais type salaire
            // relation2.ajouterTuple(new Object[] { 40, "Alice", 30000.0, 21.0, "INACTIF"
            // }); // note > 20
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur (Relation 2): " + e.getMessage());
        }

        relation2.afficherRelation();

        System.out.println("==============Union==============");

        Relation union = relation.union(relation2);
        union.afficherRelation();

        System.out.println("================Intersection=============");
        Relation inter = relation.intersection(union);
        inter.afficherRelation();

        System.out.println("=================Projection================");
        Relation projection = relation.projection(new String[] { "Name" });
        projection.afficherRelation();

        System.out.println("==================Selection================");
        Relation sel = relation.selection("Note > 10 AND (Age > 18 OR SALAIRE > 50000)");   
        sel.afficherRelation();

    }
}
