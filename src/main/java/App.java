import org.hibernate.Criteria;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import java.util.List;

public class App {
    public static void main( String[] args ) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATest");
        EntityManager em = emf.createEntityManager();
        try {
            // #1
            System.out.println("------------------ #1 ------------------");

            em.getTransaction().begin();
            try {
                Course course1 = new Course("Course-1");
                Course course2 = new Course("Course-2");
                Course course3 = new Course("Course-3");
                Course[] courses = {course1, course2, course3};

                course3.setNote("Transient note text");

                for (int i = 0; i < 15; i++) {
                    Client client = new Client("Client-" + i, i);
                    Course course = courses[i % courses.length];
                    course.addClient(client);
                }

                for (Course c : courses)
                    em.persist(c);

                em.getTransaction().commit();
            } catch (Exception ex) {
                em.getTransaction().rollback();
                return;
            }

            // #2
            System.out.println("------------------ #2 ------------------");

            Query query = em.createQuery("SELECT c FROM Client c WHERE c.age < 5", Client.class);
            List<Client> clientList = query.getResultList();

            for (Client client : clientList) {
                System.out.println(client);
            }

            // #3
            System.out.println("------------------ #3 ------------------");

            query = em.createNamedQuery("Course.findAll", Course.class);
            List<Course> courseList = query.getResultList();

            for (Course course : courseList) {
                for (Client client : course.getClients()) {
                    System.out.println(client.getName() + " goes to " + course.getName());
                }
            }

            // #4
            System.out.println("------------------ #4 ------------------");
            try {
                query = em.createNamedQuery("Course.findByName", Course.class);
                query.setParameter("name", "Course-1");
                Course course = (Course) query.getSingleResult();

                for (Client c : course.getClients()) {
                    System.out.println(c);
                }
            } catch (NoResultException ex) {
                System.out.println("Course not found!");
                return;
            } catch (NonUniqueResultException ex) {
                System.out.println("Non unique course found!");
                return;
            }

            // #5
            System.out.println("------------------ #5 ------------------");

            //Beginning of the added code.
            query = em.createQuery("SELECT c FROM Course c", Course.class);
            List<Course> list = query.getResultList();
            System.out.println(list);
            for (Course course : list) {
                System.out.printf("There is %d students at course %s.", countGroupMembers(em, course.getId()), course.toString());
                System.out.println("");
            }
            //End of the added code.

        } finally {
            em.close();
            emf.close();
        }
    }

    /**
     * Method counts amount of students at course with certain id.
     * @param em
     * @param courseId
     * @return returns number of students from group.
     */
    private static long countGroupMembers(EntityManager em, long courseId) {
        long result;

        Query query = em.createQuery("SELECT count(c) FROM Client c INNER JOIN c.courses course WHERE course.id = :a");//
        query.setParameter("a", courseId);

        result = (long) query.getSingleResult();

        return result;
    }
}
