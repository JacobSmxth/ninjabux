package com.example.NinjaBux.exception;



// not implemented, still dont know if needed.
public class CategoryRestrictedException extends RuntimeException {
    private final String itemCategory;
    private final String restrictedCategory;

    public CategoryRestrictedException(String itemCategory, String restrictedCategory) {
        super(String.format("Item category '%s' is restricted for ninjas with category '%s'", itemCategory, restrictedCategory));
        this.itemCategory = itemCategory;
        this.restrictedCategory = restrictedCategory;
    }

    public CategoryRestrictedException(String message) {
        super(message);
        this.itemCategory = null;
        this.restrictedCategory = null;
    }

    public String getItemCategory() { return itemCategory; }
    public String getRestrictedCategory() { return restrictedCategory; }
}

