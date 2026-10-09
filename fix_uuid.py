import re

with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'r') as f:
    content = f.read()

country_logic = """
        Country country = null;
        if (input.getCountry() != null && !input.getCountry().isBlank()) {
            try {
                country = countryRepository.findById(UUID.fromString(input.getCountry())).orElse(null);
            } catch (IllegalArgumentException e) {
                country = countryRepository.findByName(input.getCountry()).orElse(null);
            }
        }
"""
category_logic = """
        Category category = null;
        if (input.getCategory() != null && !input.getCategory().isBlank()) {
            try {
                category = categoryRepository.findById(UUID.fromString(input.getCategory())).orElse(null);
            } catch (IllegalArgumentException e) {
                category = categoryRepository.findByName(input.getCategory()).orElse(null);
            }
        }
"""

content = re.sub(r'Country country = null;.*?if \(input\.getCountry.*?\}.*?\}', country_logic.strip(), content, flags=re.DOTALL)
content = re.sub(r'Category category = null;.*?if \(input\.getCategory.*?\}.*?\}', category_logic.strip(), content, flags=re.DOTALL)

with open('src/main/java/com/donations/donations/service/SaveCreatorProfileUseCase.java', 'w') as f:
    f.write(content)
