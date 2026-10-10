package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;

//Import pour lancement H2 en web
import org.h2.tools.Server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;

public class App {
    public static void main(String[] args) {

        //bloc try-catch
        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

        // Création de l'entité
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-demo");

        // Insertion
        insererProduits(emf);

        // Lecture
        lireProduits(emf);

        // 1. Mise à jour du prix
        mettreAJourPrix(emf, 1L, new BigDecimal("899.99"));

        // 4. Recherche par plage de prix
        rechercherParPrix(emf, new BigDecimal("250"), new BigDecimal("500"));

        // 2. Suppression d'un produit
        supprimerProduit(emf, 3L);

        // Lecture finale pour vérifier les changements
        lireProduits(emf);

        // Fermeture
        emf.close();
    }

    private static void insererProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // 3. Création des catégories (à persister avant les produits)
            Categorie info = new Categorie("Informatique");
            Categorie mobile = new Categorie("Mobile");
            em.persist(info);
            em.persist(mobile);

            // Création produits
            Produit p1 = new Produit("Laptop", new BigDecimal("999.99"), info);
            Produit p2 = new Produit("Smartphone", new BigDecimal("499.99"), mobile);
            Produit p3 = new Produit("Tablette", new BigDecimal("299.99"), mobile);

            // Persistance produits
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            em.getTransaction().commit();
            System.out.println("Les produits se sont insérés avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // Requête récupération tous les produits
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class)
                    .getResultList();

            System.out.println("\nListe des produits :");
            for (Produit produit : produits) {
                System.out.println(produit);
            }

            // Recherche produit par ID
            System.out.println("\nRecherche du produit avec ID=2 :");
            Produit produit = em.find(Produit.class, 2L);
            if (produit != null) {
                System.out.println(produit);
            } else {
                System.out.println("Produit non trouvé");
            }
        } finally {
            em.close();
        }
    }

    // 1. Mise à jour du prix d'un produit
    private static void mettreAJourPrix(EntityManagerFactory emf, Long id, BigDecimal nouveauPrix) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                produit.setPrix(nouveauPrix); // Hibernate détecte la modification
                System.out.println("\nPrix mis à jour : " + produit);
            } else {
                System.out.println("\nProduit introuvable (id=" + id + ")");
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // 2. Suppression d'un produit
    private static void supprimerProduit(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.remove(produit);
                System.out.println("\nProduit supprimé (id=" + id + ")");
            } else {
                System.out.println("\nProduit introuvable (id=" + id + ")");
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // 4. Recherche de produits par plage de prix
    private static void rechercherParPrix(EntityManagerFactory emf, BigDecimal min, BigDecimal max) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Produit> resultats = em.createQuery(
                            "SELECT p FROM Produit p WHERE p.prix BETWEEN :min AND :max", Produit.class)
                    .setParameter("min", min)
                    .setParameter("max", max)
                    .getResultList();

            System.out.println("\nProduits entre " + min + " et " + max + " :");
            for (Produit p : resultats) {
                System.out.println(p);
            }
        } finally {
            em.close();
        }
    }
}