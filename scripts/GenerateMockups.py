"""Generate the simplified Grappe mockups as vector PDF/SVG and a local gallery.

Run with Python containing reportlab. PNG previews are rendered from the PDF
using pdftoppm; no Android build or network connection is required.
"""
from pathlib import Path
from html import escape
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import json

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'doc' / 'maquettes-simplifiees'
OUT.mkdir(parents=True, exist_ok=True)
PDF = ROOT / 'output' / 'pdf' / 'Grappe-maquettes-simplifiees.pdf'
PDF.parent.mkdir(parents=True, exist_ok=True)
for name, file in [('Body','arial.ttf'), ('Bold','arialbd.ttf'), ('Title','georgia.ttf')]:
    pdfmetrics.registerFont(TTFont(name, str(Path('C:/Windows/Fonts') / file)))
C = {'cream':'#F0EBE2', 'paper':'#FBF8F2', 'ink':'#39332E', 'muted':'#72695F',
     'wine':'#601D30', 'border':'#D4C9BB', 'rose':'#E9DADC', 'white':'#FFFFFF'}

class Drawing:
    def __init__(self, width, height, pdf=None):
        self.w, self.h, self.pdf = width, height, pdf
        self.svg = []
    def rect(self, x,y,w,h,fill='cream',r=0,stroke=None):
        fill=C.get(fill,fill); stroke=C.get(stroke,stroke)
        self.svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke or "none"}"/>')
        if self.pdf:
            self.pdf.setFillColor(fill)
            if stroke: self.pdf.setStrokeColor(stroke)
            self.pdf.roundRect(x,self.h-y-h,w,h,r,stroke=bool(stroke),fill=1)
    def text(self,x,y,text,size=14,font='Body',color='ink'):
        color=C.get(color,color)
        family={'Body':'Arial','Bold':'Arial','Title':'Georgia'}[font]
        self.svg.append(f'<text x="{x}" y="{y}" fill="{color}" font-family="{family}" font-size="{size}" font-weight="{"bold" if font=="Bold" else "normal"}">{escape(str(text))}</text>')
        if self.pdf:
            self.pdf.setFillColor(color); self.pdf.setFont(font,size)
            self.pdf.drawString(x,self.h-y,str(text))
    def center(self,x,y,text,size=14,font='Body',color='ink'):
        self.text(x-pdfmetrics.stringWidth(text,font,size)/2,y,text,size,font,color)
    def line(self,x,y,xx,yy,color='border',width=1):
        color=C.get(color,color)
        self.svg.append(f'<path d="M{x} {y}L{xx} {yy}" stroke="{color}" stroke-width="{width}" fill="none"/>')
        if self.pdf:
            self.pdf.setStrokeColor(color); self.pdf.setLineWidth(width)
            self.pdf.line(x,self.h-y,xx,self.h-yy)
    def icon(self,x,y,kind,color='wine'):
        paths={
            'home':[(0,9,9,1),(9,1,18,9),(3,7,3,19),(3,19,15,19),(15,19,15,7),(7,19,7,12),(7,12,11,12),(11,12,11,19)],
            'plus':[(2,10,18,10),(10,2,10,18)],
            'person':[(5,3,13,3),(13,3,13,10),(13,10,5,10),(5,10,5,3),(1,19,1,15),(1,15,17,15),(17,15,17,19),(17,19,1,19)],
            'edit':[(2,17,5,10),(5,10,15,0),(15,0,19,4),(19,4,9,14),(9,14,2,17)],
            'trash':[(3,5,17,5),(6,2,14,2),(5,7,5,20),(5,20,15,20),(15,20,15,7),(8,9,8,17),(12,9,12,17)],
            'search':[(3,2,12,2),(12,2,12,12),(12,12,2,12),(2,12,2,2),(12,12,19,19)],
            'refresh':[(17,6,13,2),(13,2,4,2),(4,2,1,10),(1,10,4,17),(4,17,13,17),(13,17,17,13),(17,6,10,6)],
        }
        for a,b,c,d in paths[kind]: self.line(x+a,y+b,x+c,y+d,color,1.6)
    def save(self,path):
        path.write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}" height="{self.h}" viewBox="0 0 {self.w} {self.h}" role="img">'+''.join(self.svg)+'</svg>',encoding='utf-8')
    def embed(self,other,x,y,scale=1):
        self.svg.append(f'<g transform="translate({x} {y}) scale({scale})">'+''.join(other.svg)+'</g>')
        if self.pdf:
            # PDF rendering uses the same screen functions at the translated origin.
            raise ValueError('Use draw_screen for PDF composition')

def button(d,y,label,solid=True,x=20,w=320):
    d.rect(x,y,w,44,'wine' if solid else 'cream',10,None if solid else 'border')
    d.center(x+w/2,y+28,label,14,'Bold','paper' if solid else 'wine')

def field(d,y,label,value='',x=20,w=320,h=52):
    d.rect(x,y,w,h,'cream',5,'border')
    if value:
        d.rect(x+10,y-6,min(w-20,pdfmetrics.stringWidth(label,'Body',11)+12),14,'cream')
        d.text(x+14,y+4,label,11,color='muted'); d.text(x+14,y+32,value,14)
    else: d.text(x+14,y+31,label,14,color='muted')

def nav(d,active):
    y=d.h-88
    d.rect(0,y,d.w,88,'paper')
    for i,(label,icon) in enumerate([('Mes vins','home'),('Ajouter','plus'),('Profil','person')]):
        x=d.w*(i+.5)/3
        if i==active: d.rect(x-30,y+8,60,30,'rose',16)
        d.icon(x-10,y+13,icon,'wine' if i==active else 'muted')
        d.center(x,y+59,label,12,'Bold' if i==active else 'Body','ink' if i==active else 'muted')
    d.rect(d.w/2-44,d.h-12,88,3,'ink',2)

def chrome(d,active=None):
    d.rect(0,0,d.w,d.h,'cream')
    d.text(22,25,'9:41',11,'Bold'); d.text(d.w-64,25,'Wi-Fi',9); d.rect(d.w-30,15,13,8,'ink',2)
    if active is not None: nav(d,active)

def library(d,tablet=False):
    chrome(d,0)
    left=(d.w-600)/2 if tablet else 20
    w=600 if tablet else 320
    d.text(left,78,'Mes vins',28,'Title'); d.icon(left+w-22,57,'refresh')
    d.text(left,105,'Les vins que j’ai bus',14,color='muted')
    d.rect(left,128,w,50,'cream',6,'border'); d.icon(left+13,143,'search','muted')
    d.text(left+43,159,'Rechercher un vin ou une région',13,color='muted')
    wines=[('Château des Rives','Rouge · 2021 · Saint-Émilion','4/5',['Fruits noirs, belle rondeur.']),
           ('Les Hauts de Loire','Blanc · 2023 · Vouvray','5/5',['Frais et floral.']),
           ('Domaine des Oliviers','Rosé · 2024 · Provence','4/5',['Léger, parfait avec un repas d’été.'])]
    for i,(name,sub,rating,notes) in enumerate(wines):
        y=195+i*160
        d.rect(left,y,w,146,'paper',14)
        d.text(left+16,y+30,name,21,'Title'); d.text(left+16,y+54,sub,12,color='muted')
        d.text(left+16,y+80,rating,18,'Title','wine'); d.text(left+16,y+106,notes[0],13)
        d.icon(left+w-78,y+114,'edit'); d.icon(left+w-39,y+114,'trash','muted')

def editor(d,tablet=False,editing=False):
    chrome(d,1)
    x=(d.w-560)/2 if tablet else 20; w=560 if tablet else 320
    d.text(x,78,'Modifier le vin' if editing else 'Ajouter un vin',28,'Title')
    field(d,105,'Nom du vin','Château des Rives' if editing else '',x,w)
    field(d,177,'Millésime','2021' if editing else '2026',x,(w-12)/2)
    d.rect(x+(w+12)/2,177,(w-12)/2,52,'cream',24,'border')
    d.center(x+(w+12)/2+(w-12)/4-5,209,'Rouge',13,'Bold','wine')
    cx=x+w-24
    d.line(cx-4,201,cx,205,'wine',1.4); d.line(cx,205,cx+4,201,'wine',1.4)
    field(d,249,'Région (facultatif)','Saint-Émilion' if editing else '',x,w)
    d.text(x,335,'Ma note : 4/5',18,'Title')
    # Stars drawn as polygons, so the exported files require no symbol font.
    import math
    for i in range(5):
        cx=x+18+i*46; cy=364
        pts=[(cx+10*(1 if j%2==0 else .43)*math.sin(j*math.pi/5), cy-10*(1 if j%2==0 else .43)*math.cos(j*math.pi/5)) for j in range(10)]
        color=C['wine']
        d.svg.append('<polygon points="'+' '.join(f'{a},{b}' for a,b in pts)+f'" fill="{color if i<4 else "none"}" stroke="{color}"/>')
        if d.pdf:
            path=d.pdf.beginPath(); path.moveTo(pts[0][0],d.h-pts[0][1])
            for a,b in pts[1:]: path.lineTo(a,d.h-b)
            path.close(); d.pdf.setStrokeColor(color); d.pdf.setFillColor(color); d.pdf.drawPath(path,stroke=1,fill=int(i<4))
    field(d,403,'Notes (facultatif)','Fruits noirs, belle rondeur.' if editing else '',x,w,92)
    button(d,519,'Enregistrer',x=x,w=w); d.center(d.w/2,598,'Annuler',13,'Bold','wine')

def profile(d):
    chrome(d,2)
    d.text(20,78,'Mon profil',28,'Title')
    d.rect(20,108,58,58,'rose',29); d.center(49,144,'TM',21,'Title','wine')
    d.text(94,131,'Thomas Martin',22,'Title'); d.text(94,154,'thomas@exemple.fr',13,color='muted')
    for x,value,label in [(65,'3','vins bus'),(180,'3','régions'),(292,'4,3','note moyenne')]:
        d.center(x,221,value,30,'Title','wine'); d.center(x,246,label,11,color='muted')
    button(d,277,'Modifier mon profil',False)
    d.line(20,347,340,347); d.text(20,387,'Mon compte',22,'Title')
    d.text(20,430,'Modifier mon mot de passe',14,'Bold','wine')
    d.text(20,477,'À propos de Grappe',14,'Bold','wine')
    button(d,513,'Se déconnecter',False)

def auth(d,register=False):
    chrome(d)
    d.text(24,165 if not register else 125,'Grappe',34,'Title','wine')
    d.text(24,195 if not register else 155,'Ma bibliothèque des vins bus',14,color='muted')
    y=230 if not register else 190
    if register: field(d,y,'Nom',x=24,w=312); y+=72
    field(d,y,'E-mail',x=24,w=312); field(d,y+76,'Mot de passe',x=24,w=312)
    d.text(36,y+148,'8 caractères minimum',11,color='muted')
    button(d,y+176,'Créer mon compte' if register else 'Se connecter',x=24,w=312)
    d.center(180,y+255,'Déjà un compte ? Se connecter' if register else 'Pas encore de compte ? S’inscrire',12,'Bold','wine')
    d.rect(136,785,88,3,'ink',2)

def about(d):
    chrome(d,2)
    d.text(20,78,'À propos de Grappe',27,'Title')
    for i,t in enumerate(['Une bibliothèque personnelle des vins bus :','un nom, un millésime, une couleur et une note.']): d.text(20,112+i*21,t,12)
    d.text(20,180,'Vos données',21,'Title')
    for i,t in enumerate(['Compte géré par Supabase Auth.','La bibliothèque affiche uniquement vos vins.','Dernier carnet consultable hors ligne.','Les écritures cloud nécessitent le réseau.']): d.text(20,211+i*21,t,12)
    d.text(20,325,'Catalogue de découverte',21,'Title')
    d.text(20,356,'Un exemple SampleAPIs, chargé à la demande.',12)
    button(d,380,'Charger un exemple de vin (API)',False)
    d.text(20,455,'Emporda 2012 · Maselva',13)
    d.text(20,501,'Projet pédagogique · version 1.0.0',12,color='muted')
    d.text(20,542,'L’abus d’alcool est dangereux pour la santé.',11,color='muted')
    d.text(20,560,'À consommer avec modération.',11,color='muted')
    button(d,597,'Revenir à mon carnet')
    nav(d,2)

screens=[('01-bibliotheque','Mes vins',library),('02-ajouter','Ajouter un vin',editor),('03-profil','Profil',profile),
         ('04-connexion','Connexion',auth),('05-inscription','Inscription',lambda d:auth(d,True)),('06-a-propos','À propos',about),
         ('07-modifier','Modifier un vin',lambda d:editor(d,editing=True)),
         ('08-tablette-bibliotheque','Bibliothèque sur tablette',lambda d:library(d,True)),
         ('09-tablette-formulaire','Formulaire sur tablette',lambda d:editor(d,True))]
for slug,title,fn in screens:
    d=Drawing(800 if 'tablette' in slug else 360,800); fn(d); d.save(OUT/(slug+'.svg'))

pdf=canvas.Canvas(str(PDF),pagesize=(1440,1100))
pdf.setTitle('Grappe - Maquettes de la version simplifiée')
pdf.setAuthor('Grappe')
pages=[('Le carnet, en trois écrans','Un seul objectif : se souvenir des vins bus.',screens[:3]),
       ('Les écrans du projet','Connexion, inscription et appel API conservés pour le cours.',screens[3:6]),
       ('Un formulaire pour deux actions','Même structure pour ajouter et modifier. Largeur limitée sur tablette.',[screens[6],screens[8]])]
for num,(heading,sub,items) in enumerate(pages,1):
    board=Drawing(1440,1100,pdf)
    board.rect(0,0,1440,1100,'#F7F5F1')
    board.text(52,48,'GRAPPE / MAQUETTES FONCTIONNELLES',12,'Bold','wine')
    board.text(52,102,heading,36,'Title'); board.text(52,135,sub,16,color='muted')
    positions=[(70,190),(540,190),(1010,190)] if len(items)==3 else [(100,190),(590,190)]
    for (slug,title,fn),(x,y) in zip(items,positions):
        width=800 if 'tablette' in slug else 360
        scale=.9 if len(items)==3 else (.9 if width==360 else .9)
        board.rect(x-5,y-5,width*scale+10,800*scale+10,'ink',25)
        pdf.saveState(); pdf.translate(x,1100-y-800*scale); pdf.scale(scale,scale)
        screen=Drawing(width,800,pdf); fn(screen); pdf.restoreState()
        board.svg.append(f'<g transform="translate({x} {y}) scale({scale})">'+''.join(screen.svg)+'</g>')
        board.text(x,y+800*scale+38,title,19,'Bold')
        if slug=='03-profil': board.text(x,y+800*scale+64,'3 indicateurs, aucune statistique supplémentaire.',12,color='muted')
        if slug=='02-ajouter': board.text(x,y+800*scale+64,'6 champs. Région et notes facultatives.',12,color='muted')
        if slug=='01-bibliotheque': board.text(x,y+800*scale+64,'Recherche et CRUD sur une liste personnelle.',12,color='muted')
    board.line(52,1032,1388,1032)
    board.text(52,1064,'Version simplifiée · 17 novembre 2026 · Données illustratives · Maquettes, pas captures de l’application',12,color='muted')
    board.text(1330,1064,f'{num:02} / 03',12,'Bold','wine')
    board.save(OUT/f'planche-{num:02}.svg'); pdf.showPage()
pdf.save()

cards=''.join(f'<article><h2>{escape(title)}</h2><a href="{slug}.svg"><img src="{slug}.svg" alt="Maquette : {escape(title)}" loading="lazy"></a><a class="source" href="{slug}.svg" download>Télécharger le SVG</a></article>' for slug,title,fn in screens)
(OUT/'index.html').write_text('''<!doctype html><html lang="fr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grappe — Maquettes simplifiées</title><style>
*{box-sizing:border-box}body{margin:0;background:#f7f5f1;color:#39332e;font:16px Arial,sans-serif}header,main,footer{max-width:1440px;margin:auto;padding:32px}header{padding-top:56px}small{color:#601d30;font-weight:bold;letter-spacing:.12em}h1{font:48px Georgia,serif;margin:16px 0}p{color:#72695f;line-height:1.6}nav{display:flex;gap:12px;flex-wrap:wrap}a{color:#601d30}nav a{padding:12px 18px;border:1px solid #d4c9bb;border-radius:8px;text-decoration:none}main{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:32px}article{min-width:0}h2{font:23px Georgia,serif}img{display:block;width:100%;max-height:680px;object-fit:contain;object-position:top;background:#e8e3db;border:1px solid #d4c9bb;border-radius:18px}.source{display:inline-block;margin-top:14px;font-size:14px}footer{border-top:1px solid #d4c9bb;font-size:14px;color:#72695f}@media(max-width:950px){main{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:600px){main{grid-template-columns:1fr}h1{font-size:36px}header,main,footer{padding:24px}}
</style><header><small>GRAPPE / VERSION SIMPLIFIÉE</small><h1>La bibliothèque des vins bus.</h1><p>Neuf vues dessinées en vectoriel, cohérentes avec la version Kotlin/Compose du 17 novembre 2026.<br>Données illustratives. Les écrans de compte représentent la variante cloud.</p><nav><a href="../../output/pdf/Grappe-maquettes-simplifiees.pdf">PDF pour l’oral</a><a href="planche-01.svg">Vue d’ensemble</a><a href="README.md">Périmètre et parcours</a></nav></header><main>'''+cards+'''</main><footer>SVG modifiables dans un éditeur vectoriel. Les aperçus et le PDF sont statiques ; cette galerie ne simule pas un backend. Aucune modification de l’application.</footer></html>''',encoding='utf-8')
(OUT/'README.md').write_text('''# Maquettes de Grappe - version simplifiée

Maquettes vectorielles du 17 novembre 2026, basées sur les écrans Compose actuels. Ouvrir `index.html` pour la galerie. Les SVG sont éditables dans un outil vectoriel ; le PDF de présentation se trouve dans `output/pdf/Grappe-maquettes-simplifiees.pdf` à la racine du projet.

## Périmètre

Neuf vues : bibliothèque, ajout, profil, connexion, inscription, À propos, modification et deux vues tablette. Le profil contient uniquement trois statistiques : vins bus, régions et note moyenne. Les trois vins illustratifs donnent trois régions et une moyenne de 4,3.

Connexion, inscription et modification de mot de passe appartiennent à la variante cloud. La démo locale propose à la place le bouton Explorer la démo. Les maquettes ne représentent pas une authentification de démonstration simulée.

## Parcours

Connexion ou inscription -> Mes vins -> Ajouter -> Enregistrer -> Mes vins. Le crayon ouvre le même formulaire en modification, avec conservation de l'identifiant. La corbeille demande confirmation avant suppression. Profil -> À propos -> bouton API externe. L'exemple API n'est pas ajouté au carnet.

Région et notes sont facultatives. Les états de chargement, d'erreur, de recherche vide, de carnet vide, de confirmation de suppression et les formulaires secondaires du compte ne sont pas dessinés séparément. Leurs comportements restent ceux de l'application.

## Exports

- `01-*.svg` à `09-*.svg` : vues individuelles.
- `planche-01.svg` à `planche-03.svg` : planches de présentation.
- `planche-01.png` à `planche-03.png` : aperçus rendus depuis le PDF.
- `index.html` : galerie locale, sans dépendance réseau.

La palette, les titres sérif, les six champs du vin et les trois entrées de navigation suivent l'interface existante. Les espacements sont harmonisés pour la présentation : ce sont des maquettes, pas des captures pixel à pixel. L'ancienne image `doc/maquettes.png` reste conservée comme référence historique.
''',encoding='utf-8')
print(PDF)
print(OUT/'index.html')
import subprocess
subprocess.run(['pdftoppm','-scale-to','1800','-png',str(PDF),str(OUT/'planche')],check=True)
for i in range(1,4):
    (OUT/f'planche-{i}.png').replace(OUT/f'planche-{i:02}.png')
