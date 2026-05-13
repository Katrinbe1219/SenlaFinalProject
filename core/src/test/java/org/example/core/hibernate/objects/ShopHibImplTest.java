package org.example.core.hibernate.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.models.District;
import org.example.core.models.Shop;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        CategoryHibImpl.class,
        ShopHibImpl.class,
        DistrictHibImpl.class,
})
@Transactional
public class ShopHibImplTest {

    private static final Logger logger = LogManager.getLogger(GoodHibImplTest.class);

    @Autowired
    DistrictHibImpl districtHib;

    @Autowired
    ShopHibImpl shopHib;

    private District district;

    @BeforeEach
    void setUp() {
        district = new District();
        district.setName("District");
        districtHib.save(district, logger);
    }

    private Shop createShop(String name){
        Shop shop = new Shop();
        shop.setName(name);
        shop.setDistrict(district);
        shop.setAddress("address");
        shopHib.save(shop, logger);
        return shop;

    }
    private Shop createShop(String name, District dis){
        Shop shop = new Shop();
        shop.setName(name);
        shop.setDistrict(dis);
        shop.setAddress("address");
        shopHib.save(shop, logger);
        return shop;

    }

    private District createDistrict(String name){
        District district = new District();
        district.setName(name);
        districtHib.save(district, logger);
        return district;
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionWithSortAndPagination")
    void findAllFullVersionWithSortAndPagination(){
        Shop shop1 = createShop("a");
        createShop("b");
        Shop shop3 = createShop("c");

        List<Shop> shops = shopHib.findAllFullVersion(2,0, BaseSortTypes.NAME_DESC,
                List.of(), List.of());

        Assertions.assertEquals(2, shops.size());
        Assertions.assertEquals(shop3.getName(), shops.get(0).getName());
        Assertions.assertFalse(shops.stream().map(Shop::getId).toList().contains(shop1.getId()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionWithShopIds")
    void findAllFullVersionWithShopIds(){
        createShop("a");
        Shop shop2 = createShop("b");
        createShop("c");

        List<Shop> shops = shopHib.findAllFullVersion(null,null, BaseSortTypes.ASC,
                List.of(shop2.getId()), List.of());

        Assertions.assertEquals(1, shops.size());
        Assertions.assertEquals(shop2.getId(), shops.get(0).getId());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionWithDistrictIds")
    void findAllFullVersionWithDistrictIds(){
        District dis = createDistrict("name");
        createShop("a", dis);
        Shop shop2 = createShop("b");
        createShop("c", dis);

        List<Shop> shops = shopHib.findAllFullVersion(null,null, BaseSortTypes.ASC,
                List.of(), List.of(dis.getId()));

        Assertions.assertEquals(2, shops.size());
        Assertions.assertFalse(shops.stream().map(Shop::getId).toList().contains(shop2.getId()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfNothing")
    void findAllFullVersionIfNothing(){
        List<Shop> shops = shopHib.findAllFullVersion(null,null, BaseSortTypes.ASC,
                List.of(), List.of());
        Assertions.assertEquals(0, shops.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionWithDistrictIds")
    void findByIdFullVersionIfExist(){
        Shop shop = createShop("a");
        Shop found = shopHib.findByIdFullVersion(shop.getId());

        Assertions.assertNotNull(found);
        Assertions.assertEquals(shop.getId(), found.getId());
    }

    @Test
    @Tag("positive")
    @DisplayName("findByIdFullVersionIfDoesNotExist")
    void findByIdFullVersionIfDoesNotExist(){
        Shop found = shopHib.findByIdFullVersion(1L);
        Assertions.assertNull(found);
    }
}
