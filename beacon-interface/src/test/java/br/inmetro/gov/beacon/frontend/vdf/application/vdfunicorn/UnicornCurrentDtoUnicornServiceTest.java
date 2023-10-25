package br.inmetro.gov.beacon.frontend.vdf.application.vdfunicorn;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@TestPropertySource("classpath:application-test.properties")
public class UnicornCurrentDtoUnicornServiceTest {

//    @Autowired
    VdfUnicornService vdfUnicornService;

//    @Test
    public void startTimeSlot() throws Exception {
        vdfUnicornService.startTimeSlot();
        vdfUnicornService.addSeed(new SeedPostDto("abc", "minha contribuicao", "http://virus.com"));
        vdfUnicornService.proceed();
    }
}