# TradingSystem

Dieses Projekt bietet ein **Handelssystem** für Minecraft, bei dem Spieler ihre **Inventarobjekte** und **Coins** mit anderen Spielern tauschen können. Es nutzt **Vault**, um Coins als Währung zu verwalten. Das System ermöglicht es, Handelsanfragen zu senden, Inventarobjekte hinzuzufügen oder zu entfernen und den Handel abzuschließen.

## Wichtige Komponenten

### 1. TradePlayer
- Diese Klasse repräsentiert einen Spieler innerhalb eines Handels. Sie enthält den Spieler und dessen Inventar.
- Es speichert die aktuellen Gegenstände sowie den Status des Handels (z. B. **UNFINISHED**, **PROCESSING**, **DONE**).
- Spieler können **Coins** (via Vault) und Gegenstände zu einem Handelsangebot hinzufügen, um mit anderen Spielern zu handeln.

### 2. Trade
- Die `Trade`-Klasse verwaltet den gesamten Handelsvorgang zwischen zwei Spielern.
- Sie ermöglicht es den Spielern, Gegenstände hinzuzufügen, Coins zu integrieren und den Status des Handels zu verwalten.
- Vor dem Abschluss eines Handels wird geprüft, ob beide Spieler genügend **Coins** und Platz im Inventar des anderen haben.

### 3. TradeManager
- Der `TradeManager` ist für die Verwaltung aller Handelsanfragen verantwortlich. Er verfolgt alle aktiven Handelsvorgänge und sorgt dafür, dass nur gültige Anfragen bearbeitet werden.
- Handelsanfragen werden über eine Einladung in Form eines Befehls (z. B. `/trade <Spieler>`) gestartet und können vom eingeladenen Spieler akzeptiert oder abgelehnt werden.

### 4. Vault-Integration
- Das System verwendet **Vault** zur Verwaltung von **Coins**. Diese Integration ermöglicht es, Coins von einem Spieler zu einem anderen zu übertragen, wenn ein Handel abgeschlossen wird.
- Der Trade-Manager überprüft vor dem Abschluss eines Handels, ob der Spieler ausreichend Coins in seinem Vault-Konto hat.
- Vault stellt sicher, dass alle Transaktionen mit Coins korrekt verarbeitet und auf den Konten der Spieler aktualisiert werden.

### 5. TradeCommand
- Diese Klasse stellt die Befehle zur Verfügung, die im Spiel verwendet werden, um Handelsanfragen zu senden und zu akzeptieren.
- Mit dem Befehl `/trade <Player>` kann ein Spieler einem anderen Spieler eine Handelsanfrage senden.
- Der eingeladene Spieler kann den Handel mit `/trade accept <Player>` annehmen und seine Angebote hinzufügen oder ändern.

## Funktionsweise des Handels

### 1. Einladung zum Handel
- Ein Spieler kann einem anderen Spieler eine Handelsanfrage senden, indem er den Befehl `/trade <Player>` eingibt.
- Der eingeladene Spieler erhält eine Nachricht und kann die Einladung annehmen oder ablehnen.

### 2. Handelsbestätigung
- Wenn der eingeladene Spieler den Handel akzeptiert, wird ein `Trade` erstellt. Beide Spieler können dann ihre Inventare einsehen und Elemente sowie **Coins** hinzufügen.
- Der Handel bleibt im Status **UNFINISHED**, bis beide Spieler bereit sind, den Handel abzuschließen.

### 3. Gegenstände und Coins hinzufügen
- Spieler können sowohl **Gegenstände** als auch **Coins** zu ihrem Handelsangebot hinzufügen. Gegenstände werden in das Handelsfenster gezogen, und Coins werden von Vault verwaltet und zum Handelswert hinzugefügt.
- Spieler können während des Handels ihre Angebote anpassen, indem sie Gegenstände entfernen oder zusätzliche Coins hinzufügen.

### 4. Abschluss des Handels
- Wenn beide Spieler mit den Bedingungen einverstanden sind, wird der Handel abgeschlossen.
- Beim Abschluss wird geprüft, ob beide Spieler genügend **Coins** haben und genügend Platz im Inventar des anderen vorhanden ist.
- Die **Coins** werden mithilfe von Vault übertragen, und die Gegenstände werden zwischen den Spielern ausgetauscht.

### 5. Fehlerbehandlung und Validierung
- Vor dem Abschluss des Handels wird überprüft, ob der Spieler genügend **Coins** auf seinem Vault-Konto hat und ob das Inventar des anderen Spielers ausreichend Platz für die Gegenstände bietet.
- Falls ein Fehler auftritt (z. B. zu wenig Platz im Inventar oder fehlende Coins), erhalten die Spieler eine entsprechende Fehlermeldung, und der Handel wird nicht abgeschlossen.

## Vault-Integration

- **Vault** ist die zentrale Komponente für die Verwaltung von **Coins**. Es stellt sicher, dass Coins sicher zwischen den Spielern übertragen werden, wenn der Handel abgeschlossen wird.
- Die **Vault-API** wird verwendet, um den Kontostand eines Spielers zu überprüfen und die Coins bei einem erfolgreichen Handel zu transferieren.
- Das System sorgt dafür, dass keine Transaktionen ohne ausreichende Coins auf einem Spielerkonto durchgeführt werden.

## Erweiterbarkeit

- Das Handelssystem kann problemlos erweitert werden, um weitere Währungen oder benutzerdefinierte Gegenstände hinzuzufügen, indem zusätzliche Logik für den Umgang mit weiteren Vault-Währungen implementiert wird.
- In Zukunft könnten auch komplexere Handelsoptionen integriert werden, wie z. B. das Tauschen von Währungen oder das Handeln mit seltenen, benutzerdefinierten Items.

## Fazit

Das Handelssystem für Minecraft bietet eine einfache und benutzerfreundliche Möglichkeit, **Gegenstände** und **Coins** zwischen Spielern auszutauschen. Mit der **Vault-Integration** werden **Coins** sicher und zuverlässig verwaltet, und das System sorgt dafür, dass alle Handelsbedingungen korrekt geprüft und ausgeführt werden. Es ist erweiterbar und flexibel, sodass es in verschiedenen Minecraft-Servern und -Wirtschaftssystemen genutzt werden kann.