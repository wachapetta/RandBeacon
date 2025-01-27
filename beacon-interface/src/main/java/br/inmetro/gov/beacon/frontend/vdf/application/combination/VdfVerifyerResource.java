package br.inmetro.gov.beacon.frontend.vdf.application.combination;

import br.inmetro.gov.beacon.frontend.vdf.application.combination.dto.VdfSlothReturnVerifiedDto;
import br.inmetro.gov.beacon.frontend.vdf.VdfSloth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
class VdfVerifyerResource {

    private final String p = "9325099249067051137110237972241325094526304716592954055103859972916682236180445434121127711536890366634971622095209473411013065021251467835799907856202363";

    @GetMapping("/combination/beacon/2.0/verify")
    ResponseEntity verify(@RequestParam(name = "y") String y,
                          @RequestParam(name = "x") String x,
                          @RequestParam(name = "iterations") int iterations){

        boolean verified = VdfSloth.mod_verif(new BigInteger(y), new BigInteger(x), iterations);
        return new ResponseEntity(new VdfSlothReturnVerifiedDto(p,y, x, iterations, verified), HttpStatus.OK);
    }

    @GetMapping("/unicorn/beacon/2.0/verify")
    ResponseEntity verifyUnicorn(@RequestParam(name = "y") String y,
                          @RequestParam(name = "x") String x,
                          @RequestParam(name = "iterations") int iterations){

        boolean verified = VdfSloth.mod_verif(new BigInteger(y), new BigInteger(x), iterations);
        return new ResponseEntity(new VdfSlothReturnVerifiedDto(p,y, x, iterations, verified), HttpStatus.OK);
    }

}
