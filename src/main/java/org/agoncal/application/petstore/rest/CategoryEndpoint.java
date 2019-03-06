package org.agoncal.application.petstore.rest;

import org.agoncal.application.petstore.model.Category;
import org.agoncal.application.petstore.util.Loggable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ejb.Stateless;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Stateless
@Path("/categories")
@Loggable
@Tag(name = "Category")
public class CategoryEndpoint
{

   // ======================================
   // =             Attributes             =
   // ======================================

   @PersistenceContext(unitName = "applicationPetstorePU")
   private EntityManager em;

   // ======================================
   // =          Business methods          =
   // ======================================

   @POST
   @Consumes( {"application/xml", "application/json"})
   @Operation(summary = "Creates a category")
   public Response createCategory(@RequestBody(required = true) Category entity)
   {
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(CategoryEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id}")
   @Operation(description = "Deletes a category by id")
   public Response deleteCategoryById(@PathParam("id") Long id)
   {
      Category entity = em.find(Category.class, id);
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @GET
   @Path("/{id}")
   @Produces( {"application/xml", "application/json"})
   @Operation(description = "Finds a category given an identifier")
   public Response findCategoryById(@PathParam("id") Long id)
   {
      TypedQuery<Category> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM Category c WHERE c.id = :entityId ORDER BY c.id", Category.class);
      findByIdQuery.setParameter("entityId", id);
      System.out.println("ID is: " + id);
      Category entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Produces( {"application/xml", "application/json"})
   @Operation(description = "Lists all the categories")
   @Timed(name = "listAllCategoriesTimed",
           description = "Monitor the time listAllCategories method takes",
           unit = MetricUnits.MILLISECONDS,
           absolute = true)
   @Metered(name = "listAllCategoriesMetered",
           unit = MetricUnits.MILLISECONDS,
           description = "Monitor the rate events occured",
           absolute = true)
   @Counted(unit = MetricUnits.NONE,
           name = "listAllCategoriesCount",
           absolute = true,
           monotonic = true,
           displayName = "read all categories",
           description = "Monitor how many times listAllCategories method was called")
   public List<Category> listAllCategories(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Category> findAllQuery = em.createQuery("SELECT DISTINCT c FROM Category c ORDER BY c.id", Category.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Category> results = findAllQuery.getResultList();
      return results;
   }

   @PUT
   @Path("/{id}")
   @Consumes( {"application/xml", "application/json"})
   @Operation(description = "Updates a category")
   public Response updateCategory(@PathParam("id") Long id, @RequestBody(required = true) Category entity)
   {
      try
      {
         entity.setId(id);
         entity = em.merge(entity);
      }
      catch (OptimisticLockException e)
      {
         return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
      }

      return Response.noContent().build();
   }
}
