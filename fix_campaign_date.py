with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.campanhas.lazy.tsx', 'r') as f:
    content = f.read()

content = content.replace("endDate: endDate ? new Date(endDate).toISOString() : new Date().toISOString(),", 
                          "endDate: endDate ? endDate.substring(0, 10) : new Date().toISOString().substring(0, 10),")
content = content.replace("startDate: new Date().toISOString(),", 
                          "startDate: new Date().toISOString().substring(0, 10),")

with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/dashboard.campanhas.lazy.tsx', 'w') as f:
    f.write(content)
