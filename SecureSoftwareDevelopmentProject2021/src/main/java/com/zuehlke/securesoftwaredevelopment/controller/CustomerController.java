package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;

import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import com.zuehlke.securesoftwaredevelopment.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller

public class CustomerController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CustomerController.class);

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers-and-restaurants")
    @PreAuthorize("hasAuthority('RESTAURANT_LIST_VIEW')")
    public String customersAndRestaurants(Model model) {
        if(SecurityUtil.hasPermission("USERS_LIST_VIEW"))
            model.addAttribute("customers", customerRepository.getCustomers());
        else
            model.addAttribute("customers",null);
        model.addAttribute("restaurants", customerRepository.getRestaurants());
        return "customers-and-restaurants";
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_DETAILS_VIEW')")
    public String getRestaurant(@RequestParam(name = "id", required = true) String id, Model model, HttpSession session) {
        String csrf =session.getAttribute("CSRF_TOKEN").toString();
        model.addAttribute("CSRF_TOKEN", csrf);
        System.out.println(model.getAttribute("CSRF_TOKEN"));
        model.addAttribute("restaurant", customerRepository.getRestaurant(id));
        return "restaurant";
    }

    @DeleteMapping("/restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_DELETE')")
    public String deleteRestaurant(@RequestParam(name = "id", required = true) String id, HttpSession session, Model model ) throws AccessDeniedException {
        int identificator = Integer.valueOf(id);
        customerRepository.deleteRestaurant(identificator);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/restaurant/update-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_EDIT')")
    public String updateRestaurant(RestaurantUpdate restaurantUpdate, Model model, HttpSession session, @RequestParam("csrfToken") String csrfToken) throws AccessDeniedException {
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            throw new AccessDeniedException("Acess Denied");
        }
        customerRepository.updateRestaurant(restaurantUpdate);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('USERS_DETAILS_VIEW')")
    public String getCustomer(@RequestParam(name = "id", required = true) String id, Model model, HttpSession session) {
        String csrf =session.getAttribute("CSRF_TOKEN").toString();
        model.addAttribute("CSRF_TOKEN", csrf);
        model.addAttribute("customer", customerRepository.getCustomer(id));
        model.addAttribute("addresses", customerRepository.getAddresses(id));
        return "customer";
    }

    @DeleteMapping("/customer")
    @PreAuthorize("hasAuthority('USERS_DELETE')")
    public String deleteCustomer(@RequestParam(name = "id", required = true) String id,HttpSession session,@RequestParam(name="csrf", required = true) String csrfToken) {
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            System.out.println("N E");
            throw new AccessDeniedException("Acess Denied");
        }
        customerRepository.deleteCustomer(id);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/customer/update-customer")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String updateCustomer(CustomerUpdate customerUpdate, Model model, HttpSession session, @RequestParam("csrfToken") String csrfToken) throws AccessDeniedException {
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            throw new AccessDeniedException("Acess Denied");
        }
        customerRepository.updateCustomer(customerUpdate);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @DeleteMapping("/customer/address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String deleteCustomerAddress(@RequestParam(name = "id", required = true) String id, HttpSession session, @RequestParam(name="csrf", required = true) String csrfToken) throws AccessDeniedException{
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            System.out.println("N E");
            throw new AccessDeniedException("Acess Denied");
        }
        int identificator = Integer.valueOf(id);
        customerRepository.deleteCustomerAddress(identificator);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/customer/address/update-address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String updateCustomerAddress(Address address, Model model, HttpSession session, @RequestParam("csrfToken") String csrfToken ) throws AccessDeniedException{
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            throw new AccessDeniedException("Acess Denied");
        }
        customerRepository.updateCustomerAddress(address);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @PostMapping("/customer/address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String putCustomerAddress(NewAddress newAddress, Model model, HttpSession session, @RequestParam("csrfToken") String csrfToken) throws AccessDeniedException{
        String sessionToken = session.getAttribute("CSRF_TOKEN").toString();
        if(!sessionToken.equals(csrfToken)){
            throw new AccessDeniedException("Acess Denied");
        }
        customerRepository.putCustomerAddress(newAddress);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }
}
