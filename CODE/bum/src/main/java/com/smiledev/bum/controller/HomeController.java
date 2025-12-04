package com.smiledev.bum.controller;

import com.smiledev.bum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private ProductVersionsRepository productVersionsRepository;
    @Autowired
    private ProductPackagesRepository productPackagesRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private LicensesRepository licensesRepository;
    @Autowired
    private TransactionsRepository transactionsRepository;
    @Autowired
    private ReviewsRepository reviewsRepository;
    @Autowired
    private ActivityLogsRepository activityLogsRepository;
    @Autowired
    private KeyValidationLogsRepository keyValidationLogsRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", usersRepository.findAll());
        model.addAttribute("products", productsRepository.findAll());
        model.addAttribute("categories", categoriesRepository.findAll());
        model.addAttribute("productVersions", productVersionsRepository.findAll());
        model.addAttribute("productPackages", productPackagesRepository.findAll());
        model.addAttribute("orders", ordersRepository.findAll());
        model.addAttribute("licenses", licensesRepository.findAll());
        model.addAttribute("transactions", transactionsRepository.findAll());
        model.addAttribute("reviews", reviewsRepository.findAll());
        model.addAttribute("activityLogs", activityLogsRepository.findAll());
        model.addAttribute("keyValidationLogs", keyValidationLogsRepository.findAll());
        return "home";
    }
}
