package br.inmetro.gov.beacon.frontend.vdf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Slf4j
public class VdfSloth {

    private static final BigInteger BIG_INT_QUATRO = new BigInteger("4");
    private static final BigInteger BIG_INT_DOIS = new BigInteger("2");
    private static final BigInteger BIG_INT_UM = new BigInteger("1");
    private static final BigInteger p = new BigInteger("9325099249067051137110237972241325094526304716592954055103859972916682236180445434121127711536890366634971622095209473411013065021251467835799907856202363"); // foi o q que funcionou melhor

    @Async("higherPriorityExecutor")
    public Future<BigInteger> mod_op(BigInteger x, int t) throws ExecutionException, InterruptedException {

        log.info("calculation");
        for (int i = 0; i < t; i++) {
            x = mod_sqrt_op(x, p);
        }
        return new AsyncResult<BigInteger>(x);
    }


    private BigInteger mod_sqrt_op(BigInteger x, BigInteger p) {
         BigInteger pmais1duv4 = p.add(BIG_INT_UM).divide( BIG_INT_QUATRO);
         BigInteger y = x.modPow(pmais1duv4,p);
         return y;
    }

    private static boolean quad_res(BigInteger x, BigInteger p) {
        BigInteger powT = x.modPow(p.subtract(BIG_INT_UM).divide(BIG_INT_DOIS), p);
        return powT.equals(BIG_INT_UM);
    }

    public static boolean mod_verif(BigInteger y, BigInteger x, int t){

        for (int i = 0; i < t; i++) {
            y = y.modPow(BIG_INT_DOIS, p);

            if (!quad_res(y, p)) {
                y = (y.negate()).mod(p);
            }
        }

        if (x.mod(p).equals(y) || (x.negate()).mod(p).equals(y)){
            return true;
        } else {
            return false;
        }
    }

}
