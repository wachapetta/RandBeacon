package br.inmetro.gov.beacon.frontend.vdf.application.vdfunicorn;

import br.inmetro.gov.beacon.frontend.vdf.repository.VdfUnicornRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/unicorn/beacon/2.0/seeds")
public class VdfUnicornController implements WebMvcConfigurer {

    private final VdfUnicornService vdfUnicornService;

    private final SmartValidator validator;

    @Autowired
    public VdfUnicornController(VdfUnicornService vdfUnicornService, VdfUnicornRepository vdfUnicornRepository, SmartValidator validator) {
        this.vdfUnicornService = vdfUnicornService;
        this.validator = validator;
    }

    @GetMapping("/new")
    public String novo(Model model){
        model.addAttribute("seedPostDto", new SeedPostDto());
        return "vdf-unicorn/form";
    }

    @PostMapping("/new")
    public String salvar(@ModelAttribute SeedPostDto seedPostDto, RedirectAttributes attributes, BindingResult bindingResult){
        validator.validate(seedPostDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return "vdf-unicorn/form";
        }

        if (!vdfUnicornService.isOpen()){
            attributes.addFlashAttribute("error", "Operation not allowed. Wait until next timeslot.");
            return "redirect:/unicorn";
        }

        vdfUnicornService.addSeed(seedPostDto);
        attributes.addFlashAttribute("message", "Seed successfully added");

        return "redirect:/unicorn";
    }
}
