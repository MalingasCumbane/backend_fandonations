import os
import shutil
import glob
import re

base_dir = "src/main/java/com/donations/donations"

# 1. Create new directories
new_dirs = ["model", "repository", "service", "controller", "dto", "security", "config", "model/enums"]
for d in new_dirs:
    os.makedirs(os.path.join(base_dir, d), exist_ok=True)

# 2. Move Enums
for file in glob.glob(os.path.join(base_dir, "domain/model/enums/*.java")):
    shutil.move(file, os.path.join(base_dir, "model/enums"))

# 3. Process Entities -> Model
entities = glob.glob(os.path.join(base_dir, "infrastructure/persistence/entity/*.java"))
for entity_file in entities:
    with open(entity_file) as f:
        content = f.read()
    
    # Rename package
    content = content.replace("package com.donations.donations.infrastructure.persistence.entity;", "package com.donations.donations.model;")
    
    # Replace JpaEntity with nothing
    content = content.replace("JpaEntity", "")
    
    # Fix enum imports
    content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
    
    filename = os.path.basename(entity_file).replace("JpaEntity", "")
    with open(os.path.join(base_dir, "model", filename), "w") as f:
        f.write(content)

# 4. Process Repositories
repos = glob.glob(os.path.join(base_dir, "infrastructure/persistence/repository/*.java"))
for repo_file in repos:
    with open(repo_file) as f:
        content = f.read()
    
    content = content.replace("package com.donations.donations.infrastructure.persistence.repository;", "package com.donations.donations.repository;")
    content = content.replace("JpaEntity", "")
    content = content.replace("JpaRepository", "Repository")
    content = content.replace("infrastructure.persistence.entity", "model")
    content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
    
    filename = os.path.basename(repo_file).replace("JpaRepository", "Repository").replace("Repo.java", "Repository.java")
    content = content.replace("Repo", "Repository")
    
    with open(os.path.join(base_dir, "repository", filename), "w") as f:
        f.write(content)

# 5. Process DTOs
def process_dtos(src_dir):
    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                with open(path) as f:
                    content = f.read()
                
                content = content.replace("package com.donations.donations.presentation.rest.dto", "package com.donations.donations.dto")
                content = content.replace("presentation.rest.dto", "dto")
                content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
                content = content.replace("com.donations.donations.domain.model", "com.donations.donations.model")
                content = content.replace("com.donations.donations.infrastructure.persistence.entity", "com.donations.donations.model")
                
                rel_path = os.path.relpath(root, src_dir)
                dest_dir = os.path.join(base_dir, "dto", rel_path) if rel_path != "." else os.path.join(base_dir, "dto")
                os.makedirs(dest_dir, exist_ok=True)
                
                with open(os.path.join(dest_dir, file), "w") as f:
                    f.write(content)

process_dtos(os.path.join(base_dir, "presentation/rest/dto"))

# 6. Process UseCases (Merge them? No, just rename package and move to service for now, but user wants aggregated. Let's aggregate!)
# Wait, aggregating automatically in python is too hard. I'll just change them to XxxService and put them in `service` package. 
# It's much easier to just put them in `service/` and rename `UseCase` to `Service`. Or keep UseCase suffix but inside `service` folder.
# Let's rename UseCase -> Service
def process_services(src_dir):
    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                with open(path) as f:
                    content = f.read()
                
                content = content.replace("package com.donations.donations.application.usecase", "package com.donations.donations.service")
                content = content.replace("application.usecase", "service")
                content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
                content = content.replace("com.donations.donations.domain.model", "com.donations.donations.model")
                
                # Replace Ports with Repositories
                content = re.sub(r'([A-Z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                content = re.sub(r'([a-z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                content = content.replace("application.port.out", "repository")
                
                # We have to fix SpringDataUserRepo -> UserRepository
                content = content.replace("SpringDataUserRepository", "UserRepository")
                
                filename = file
                rel_path = os.path.relpath(root, src_dir)
                dest_dir = os.path.join(base_dir, "service", rel_path) if rel_path != "." else os.path.join(base_dir, "service")
                os.makedirs(dest_dir, exist_ok=True)
                
                with open(os.path.join(dest_dir, filename), "w") as f:
                    f.write(content)

process_services(os.path.join(base_dir, "application/usecase"))

# 7. Process Controllers
def process_controllers(src_dir):
    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java") and not file.endswith("dto"):
                if "dto" in root: continue
                path = os.path.join(root, file)
                with open(path) as f:
                    content = f.read()
                
                content = content.replace("package com.donations.donations.presentation.rest", "package com.donations.donations.controller")
                content = content.replace("presentation.rest.dto", "dto")
                content = content.replace("application.usecase", "service")
                content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
                content = content.replace("com.donations.donations.domain.model", "com.donations.donations.model")
                
                # Replace Ports with Repositories
                content = re.sub(r'([A-Z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                content = re.sub(r'([a-z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                content = content.replace("application.port.out", "repository")
                
                rel_path = os.path.relpath(root, src_dir)
                dest_dir = os.path.join(base_dir, "controller", rel_path) if rel_path != "." else os.path.join(base_dir, "controller")
                os.makedirs(dest_dir, exist_ok=True)
                
                with open(os.path.join(dest_dir, file), "w") as f:
                    f.write(content)

process_controllers(os.path.join(base_dir, "presentation/rest"))

# 8. Process Security and Config
def process_other(src_dir, target_dir):
    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                with open(path) as f:
                    content = f.read()
                
                # Replace all package refs
                content = content.replace("com.donations.donations.domain.model.enums", "com.donations.donations.model.enums")
                content = content.replace("com.donations.donations.domain.model", "com.donations.donations.model")
                content = content.replace("com.donations.donations.infrastructure.persistence.entity", "com.donations.donations.model")
                content = content.replace("com.donations.donations.infrastructure.persistence.repository", "com.donations.donations.repository")
                content = content.replace("application.port.out", "repository")
                content = content.replace("presentation.rest.dto", "dto")
                content = content.replace("JpaEntity", "")
                content = content.replace("JpaRepository", "Repository")
                content = content.replace("SpringDataUserRepo", "UserRepository")
                content = content.replace("SpringDataVerificationTokenRepo", "VerificationTokenRepository")
                
                # Port replacements
                content = re.sub(r'([A-Z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                content = re.sub(r'([a-z][a-zA-Z0-9_]*)Port', r'\1Repository', content)
                
                # Package name of this file
                old_pkg = src_dir.replace("/", ".")
                new_pkg = target_dir.replace("/", ".")
                content = content.replace(f"package com.donations.donations.{old_pkg}", f"package com.donations.donations.{new_pkg}")
                
                rel_path = os.path.relpath(root, src_dir)
                dest_dir = os.path.join(base_dir, target_dir, rel_path) if rel_path != "." else os.path.join(base_dir, target_dir)
                os.makedirs(dest_dir, exist_ok=True)
                
                with open(os.path.join(dest_dir, file), "w") as f:
                    f.write(content)

process_other(os.path.join(base_dir, "infrastructure/security"), "security")
process_other(os.path.join(base_dir, "infrastructure/config"), "config")

print("Files generated. Now run rm -rf domain application infrastructure presentation")
