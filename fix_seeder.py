import re

path = "src/main/java/com/donations/donations/infrastructure/config/DatabaseSeeder.java"
with open(path) as f: content = f.read()

old_logic = """        categoryRepository.deleteAll();
        countryRepository.deleteAll();

        List<String> cats = List.of(
            "Influencer", "Tiktoker", "Youtuber", "Jornalismo", "Música", "Entretenimento"
        );
        
        for (String c : cats) {
            CategoryJpaEntity ent = new CategoryJpaEntity();
            ent.setName(c);
            categoryRepository.save(ent);
        }

        List<String[]> countries = List.of(
            new String[]{"Moçambique", "MZ", "+258"},
            new String[]{"África do Sul", "ZA", "+27"},
            new String[]{"Senegal", "SN", "+221"},
            new String[]{"Botswana", "BW", "+267"},
            new String[]{"USA", "US", "+1"},
            new String[]{"Canada", "CA", "+1"}
        );
        
        for (String[] c : countries) {
            CountryJpaEntity ent = new CountryJpaEntity();
            ent.setName(c[0]);
            ent.setCode(c[1]);
            ent.setPhoneCode(c[2]);
            countryRepository.save(ent);
        }"""

new_logic = """        if (categoryRepository.count() == 0) {
            List<String> cats = List.of(
                "Influencer", "Tiktoker", "Youtuber", "Jornalismo", "Música", "Entretenimento"
            );
            
            for (String c : cats) {
                CategoryJpaEntity ent = new CategoryJpaEntity();
                ent.setName(c);
                categoryRepository.save(ent);
            }
        }

        if (countryRepository.count() == 0) {
            List<String[]> countries = List.of(
                new String[]{"Moçambique", "MZ", "+258"},
                new String[]{"África do Sul", "ZA", "+27"},
                new String[]{"Senegal", "SN", "+221"},
                new String[]{"Botswana", "BW", "+267"},
                new String[]{"USA", "US", "+1"},
                new String[]{"Canada", "CA", "+1"}
            );
            
            for (String[] c : countries) {
                CountryJpaEntity ent = new CountryJpaEntity();
                ent.setName(c[0]);
                ent.setCode(c[1]);
                ent.setPhoneCode(c[2]);
                countryRepository.save(ent);
            }
        }"""

content = content.replace(old_logic, new_logic)

# To avoid problems if already replaced:
with open(path, "w") as f: f.write(content)
