# Define variables
YML_FILE := .gitlab-ci.yml

# Default target
.PHONY: all
all: modify clean

# Target to modify the args in .gitlab-ci.yml
.PHONY: modify
modify:
	@echo "Modifying the .gitlab-ci.yml file..."
	./modify_args.sh $(ARG1) $(ARG2)

# Clean backup files
.PHONY: clean
clean:
	@echo "Cleaning up backup files..."
	rm -f $(YML_FILE).bak
