package de.bahmut.kindleproxy.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BrowseController {

    @GetMapping("/")
    public String home(
            final Model model
    ) {
        model.addAttribute("title", "Homepage");
        return "index";
    }

}
