# TradingSystem

Dieses Projekt bietet ein **Handelssystem** für Minecraft, bei dem Spieler ihre **Inventarobjekte** und **Coins** mit anderen Spielern tauschen können. Es nutzt **Vault**, um Coins als Währung zu verwalten. Das System ermöglicht es, Handelsanfragen zu senden, Inventarobjekte hinzuzufügen oder zu entfernen und den Handel abzuschließen.

## Vault-Integration

- **Vault** ist die zentrale Komponente für die Verwaltung von **Coins**. Es stellt sicher, dass Coins sicher zwischen den Spielern übertragen werden, wenn der Handel abgeschlossen wird.
- Die **Vault-API** wird verwendet, um den Kontostand eines Spielers zu überprüfen und die Coins bei einem erfolgreichen Handel zu transferieren.
- Das System sorgt dafür, dass keine Transaktionen ohne ausreichende Coins auf einem Spielerkonto durchgeführt werden.

## Wichtige Komponenten

### 1. TradePlayer
- Diese Klasse repräsentiert einen Spieler innerhalb eines Handels. Sie enthält den Spieler und dessen Inventar.
- Es speichert die aktuellen Gegenstände sowie den Status des Handels (z. B. **UNFINISHED**, **PROCESSING**, **DONE**).
- Spieler können **Coins** (via Vault) und Gegenstände zu einem Handelsangebot hinzufügen, um mit anderen Spielern zu handeln.

```java
public class TradePlayer {

    private final Player player;
    private Trade.State state;
    private final ArrayList<ItemStack> items;

    private Optional<Integer> coins;
    private Optional<Integer> value;

    public TradePlayer(@NonNull Player player) {
        this.player = player;
        this.state = Trade.State.UNFINISHED;
        this.items = new ArrayList<>();
        this.coins = Optional.of(0);
        this.value = Optional.of(1);
    }
    
    // Weitere Methoden zum Hinzufügen und Entfernen von Items und Coins
}
```

### 2. Trade
- Der `Trade`-Record verwaltet den gesamten Handelsvorgang zwischen zwei Spielern.
- Er ermöglicht es den Spielern, Gegenstände hinzuzufügen, Coins zu integrieren und den Status des Handels zu verwalten.
- Vor dem Abschluss eines Handels wird geprüft, ob beide Spieler genügend **Coins** und Platz im Inventar des anderen haben.

  
```java
public record Trade(@NonNull TradePlayer host, @NonNull TradePlayer target) implements TradeActions {

    public Trade(@NonNull TradePlayer host, @NonNull TradePlayer target) {
        this.host = host;
        this.target = target;
        Arrays.asList(host, target).forEach(this::createInventory);
    }
}
```

### 3. Caching der Handelslogs

Um die Leistung zu verbessern und wiederholte Anfragen zu optimieren, verwendet das System ein **Caching** für die Handelslogs. Hierbei wird eine **Cache-Größe** von maximal **200 Trades** pro Spieler verwendet. Wenn mehr als 200 Handelslogs für einen Spieler im Cache gespeichert werden, wird das älteste Log entfernt, um Platz für neue Einträge zu schaffen.

### Funktionsweise des Caching:
- Jeder Spieler hat eine Liste von Handelslogs, die im Cache gespeichert werden.
- Wenn ein Spieler nach seinen Handelslogs fragt, wird zuerst der Cache überprüft.
- Wenn die Logs nicht im Cache vorhanden sind, werden sie aus der Konfigurationsdatei geladen und anschließend im Cache gespeichert.
- Der Cache sorgt dafür, dass nicht jedes Mal die komplette Konfigurationsdatei geladen werden muss, was die Abfragegeschwindigkeit verbessert.

### Maximale Cache-Größe:
- Es werden maximal **200 Handelslogs** pro Spieler im Cache gehalten.
- Wenn der Cache voll ist und ein weiteres Handelslog hinzugefügt werden soll, wird das älteste Log aus dem Cache entfernt, um Platz für den neuen Eintrag zu schaffen.

```java
private static final int MAX_CACHE_SIZE = 200;

private final Map<UUID, List<String>> tradeLogCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<UUID, List<String>> eldest) {
        return size() > MAX_CACHE_SIZE;
    }
};
```

### 4. TradeManager
- Der `TradeManager` ist für die Verwaltung aller Handelsanfragen verantwortlich. Er verfolgt alle aktiven Handelsvorgänge und sorgt dafür, dass nur gültige Anfragen bearbeitet werden.
- Handelsanfragen werden über eine Einladung in Form eines Befehls (z. B. `/trade <Spieler>`) gestartet und können vom eingeladenen Spieler akzeptiert oder abgelehnt werden.

### 5. Vault-Integration
- Das System verwendet **Vault** zur Verwaltung von **Coins**. Diese Integration ermöglicht es, Coins von einem Spieler zu einem anderen zu übertragen, wenn ein Handel abgeschlossen wird.
- Der Trade-Manager überprüft vor dem Abschluss eines Handels, ob der Spieler ausreichend Coins in seinem Vault-Konto hat.
- Vault stellt sicher, dass alle Transaktionen mit Coins korrekt verarbeitet und auf den Konten der Spieler aktualisiert werden.

### 6. TradeCommand
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
