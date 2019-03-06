package org.agoncal.application.petstore.rest;

import org.agoncal.application.petstore.model.Country;
import org.agoncal.application.petstore.util.Loggable;
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
@Path("/countries")
@Loggable
@Tag(name = "Country")
public class CountryEndpoint
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
   @Operation(description = "Creates a country")
   public Response createCountry(@RequestBody(required = true) Country entity)
   {
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(CountryEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id}")
   @Operation(description = "Deletes a country given an id")
   public Response deleteCountryById(@PathParam("id") Long id)
   {
      Country entity = em.find(Country.class, id);
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
   @Operation(description = "Retrieves a country by its id")
   public Response findCountryById(@PathParam("id") Long id)
   {
      TypedQuery<Country> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM Country c WHERE c.id = :entityId ORDER BY c.id", Country.class);
      findByIdQuery.setParameter("entityId", id);
      Country entity;
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
   @Operation(description = "Lists all the countries")
   public List<Country> listAllCountries(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Country> findAllQuery = em.createQuery("SELECT DISTINCT c FROM Country c ORDER BY c.id", Country.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Country> results = findAllQuery.getResultList();
      return results;
   }

   @PUT
   @Path("/{id}")
   @Consumes( {"application/xml", "application/json"})
   @Operation(description = "Updates a country")
   public Response updateCountry(@PathParam("id")Long id, @RequestBody(required = true) Country entity)
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
