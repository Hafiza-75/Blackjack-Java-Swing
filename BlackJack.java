import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    private class Card {
        String value, type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("JQK".contains(value)) return 10;
            if ("A".equals(value)) return 11;
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return "A".equals(value);
        }

        public String getImagePath() {
            return "/cards/" + toString() + ".png"; // Changed to use resource path
        }
    }

    ArrayList<Card> deck;
    Random random = new Random();

    // Dealer and Player
    Card hiddenCard;
    ArrayList<Card> dealerHand, playerHand;
    int dealerSum, dealerAceCount;
    int playerSum, playerAceCount;

    // Window
    final int boardWidth = 800;
    final int boardHeight = 600;
    final int cardWidth = 90;
    final int cardHeight = 130;

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(new Color(7, 99, 36)); // Dark green background

            try {
                // Draw dealer's hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("/cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 100, 50, cardWidth, cardHeight, null);

                // Draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Image cardImg = new ImageIcon(getClass().getResource(dealerHand.get(i).getImagePath())).getImage();
                    g.drawImage(cardImg, 100 + (cardWidth + 10) * (i + 1), 50, cardWidth, cardHeight, null);
                }

                // Draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Image cardImg = new ImageIcon(getClass().getResource(playerHand.get(i).getImagePath())).getImage();
                    g.drawImage(cardImg, 100 + (cardWidth + 10) * i, 300, cardWidth, cardHeight, null);
                }

                g.setFont(new Font("Verdana", Font.BOLD, 20));
                g.setColor(Color.WHITE);
                g.drawString("Dealer Score: " + (stayButton.isEnabled() ? "??" : reduceDealerAce()), 100, 30);
                g.drawString("Your Score: " + reducePlayerAce(), 100, 280);

                // Game result
                if (!stayButton.isEnabled()) {
                    String result = getResultMessage();
                    g.setFont(new Font("Verdana", Font.BOLD, 30));
                    g.setColor(Color.YELLOW);
                    g.drawString(result, boardWidth / 2 - 100, 250);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    JPanel buttonPanel = new JPanel();
    JButton hitButton = createStyledButton("Hit");
    JButton stayButton = createStyledButton("Stay");
    JButton playAgainButton = createStyledButton("Play Again");

    BlackJack() {
        startGame();

        frame.setSize(boardWidth, boardHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        gamePanel.setLayout(null);
        frame.add(gamePanel, BorderLayout.CENTER);

        buttonPanel.setBackground(new Color(0, 80, 30));
        buttonPanel.add(hitButton);
        buttonPanel.add(stayButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        hitButton.addActionListener(e -> {
            Card card = drawCard(playerHand);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            if (reducePlayerAce() > 21) {
                hitButton.setEnabled(false);
            }
            gamePanel.repaint();
        });

        stayButton.addActionListener(e -> {
            hitButton.setEnabled(false);
            stayButton.setEnabled(false);
            dealerPlay();
            gamePanel.repaint();
            buttonPanel.add(playAgainButton);
            frame.revalidate();
        });

        playAgainButton.addActionListener(e -> {
            frame.dispose();
            new BlackJack();
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setBackground(new Color(17, 122, 39));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Verdana", Font.BOLD, 18));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        dealerSum = dealerAceCount = 0;
        playerSum = playerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card visibleCard = deck.remove(deck.size() - 1);
        dealerSum += visibleCard.getValue();
        dealerAceCount += visibleCard.isAce() ? 1 : 0;
        dealerHand.add(visibleCard);

        for (int i = 0; i < 2; i++) {
            Card card = deck.remove(deck.size() - 1);
            playerHand.add(card);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
        }
    }

    private Card drawCard(ArrayList<Card> hand) {
        Card card = deck.remove(deck.size() - 1);
        hand.add(card);
        return card;
    }

    private void dealerPlay() {
        while (reduceDealerAce() < 17) {
            Card card = drawCard(dealerHand);
            dealerSum += card.getValue();
            dealerAceCount += card.isAce() ? 1 : 0;
        }
    }

    private void buildDeck() {
        deck = new ArrayList<>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (String type : types) {
            for (String value : values) {
                deck.add(new Card(value, type));
            }
        }
    }

    private void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, temp);
        }
    }

    private int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount--;
        }
        return playerSum;
    }

    private int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount--;
        }
        return dealerSum;
    }

    private String getResultMessage() {
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();

        if (playerSum > 21) return "You Bust!";
        if (dealerSum > 21) return "Dealer Busts! You Win!";
        if (playerSum == dealerSum) return "Push! It's a Tie!";
        if (playerSum > dealerSum) return "You Win!";
        return "Dealer Wins!";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BlackJack::new);
    }
}
