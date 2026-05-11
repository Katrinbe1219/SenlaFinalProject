package org.example.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.core.mapping.users.ProfileDtoMapper;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.JwtService;
import org.example.core.security.UserDetailsCustomService;
import org.example.core.services.RecalculationService;
import org.example.core.services.auth.AuthService;
import org.example.core.services.auth.RefreshTokenService;
import org.example.core.services.dictionaries.CategoryService;
import org.example.core.services.dictionaries.DistrictService;
import org.example.core.services.dictionaries.TagService;
import org.example.core.services.documents.FavouriteService;
import org.example.core.services.documents.ModeratorRecalcService;
import org.example.core.services.documents.RateService;
import org.example.core.services.documents.prices.PriceAnalyzeService;
import org.example.core.services.documents.prices.PriceExportService;
import org.example.core.services.documents.prices.PriceService;
import org.example.core.services.documents.reviews.ReviewAdvancedService;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
import org.example.core.services.export.ExportCsvService;
import org.example.core.services.export.ExportXlsxService;
import org.example.core.services.graphics.GraphicalAnalyseService;
import org.example.core.services.objects.GoodService;
import org.example.core.services.objects.ShopService;
import org.example.core.services.objects.UserService;
import org.example.core.services.upload.ImportFileCsvService;
import org.example.core.services.upload.ImportFileXlsxService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Clock;

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
public class TestServicesConfig {
    @Bean
    public PriceAnalyzeService priceAnalyzeService(){
        return mock(PriceAnalyzeService.class);
    }

    @Bean
    public PriceService priceService(){
        return mock(PriceService.class);
    }

    @Bean
    public ImportFileCsvService importFileCsvService(){
        return mock(ImportFileCsvService.class);
    }

    @Bean
    public ImportFileXlsxService importFileXlsxService(){
        return mock(ImportFileXlsxService.class);
    }
    @Bean
    public PriceExportService priceExportService(){
        return mock(PriceExportService.class);
    }

    @Bean
    public ExportCsvService csvService(){
        return mock(ExportCsvService.class);
    }

    @Bean
    public ExportXlsxService xlsxService(){
        return mock(ExportXlsxService.class);
    }

    @Bean
    public RateService rateService() {
        return Mockito.mock(RateService.class);
    }

    @Bean
    public RecalculationService recalculationService() {
        return Mockito.mock(RecalculationService.class);
    }

    @Bean
    public GraphicalAnalyseService graphicalAnalyseService() {
        return Mockito.mock(GraphicalAnalyseService.class);
    }

    @Bean
    public ReviewAdvancedService reviewService(){
        return mock(ReviewAdvancedService.class);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ProfileDtoMapper profileDtoMapper(){
        return mock(ProfileDtoMapper.class);
    }
    @Bean
    public UserService userService(){
        return mock(UserService.class);
    }

    @Bean
    public AuthService authService(){
        return mock(AuthService.class);
    }

    @Bean
    public ModeratorRecalcService moderatorRecalcService() {
        return mock(ModeratorRecalcService.class);
    }
    @Bean
    public PriceSubService priceSubService() {
        return mock(PriceSubService.class);
    }

    @Bean
    public AvailabilitySubService availabilitySubService() {
        return mock(AvailabilitySubService.class);
    }

    @Bean
    public TagService tagService() {
        return mock(TagService.class);
    }
    @Bean
    public DistrictService districtService() {
        return mock(DistrictService.class);
    }

    @Bean
    public ShopService shopService() {
        return mock(ShopService.class);
    }
    @Bean
    @Primary
    public GoodService goodService() {
        return mock(GoodService.class);
    }

    @Bean
    @Primary
    public CategoryService categoryService() {
        return mock(CategoryService.class);
    }

    @Bean
    @Primary
    public ReviewForUserService reviewForUserService() {
        return mock(ReviewForUserService.class);
    }

    @Bean
    @Primary
    public FavouriteService favouriteService() {
        return mock(FavouriteService.class);
    }

    @Bean
    @Primary
    public RefreshTokenService refreshTokenService() {
        return mock(RefreshTokenService.class);
    }

    @Bean
    @Primary
    public DeviceInfoExtractor deviceInfoExtractor() {
        return mock(DeviceInfoExtractor.class);
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsCustomService.class);
    }
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return mock(ObjectMapper.class);
    }
}
