#!/bin/bash

# Base URL for the API (replace with your actual base URL)
BASE_URL="http://localhost:8080/api/v1"  # Change this to the real base URL, as '_base_url' seems like a placeholder

# User IDs
USER1="4c470587-0119-11f1-ac8a-8c1d966630f0"  # Triwa Leearn - Theme: Education and Learning
USER2="6697644d-01b5-11f1-94ac-8c1d966630f0"  # Red Trips - Theme: Travel Adventures
USER3="6ff2fab3-01b5-11f1-94ac-8c1d966630f0"  # Gurt Swerr - Theme: Food and Culture

# Function to create a post via curl
create_post() {
  local userId="$1"
  local title="$2"
  local content="$3"
  local status="Publish"

  curl -X POST "$BASE_URL/posts" \
    -H "Content-Type: application/json" \
    -d '{
      "userId": "'"$userId"'",
      "title": "'"$title"'",
      "content": "'"$content"'",
      "status": "'"$status"'"
    }'
  echo ""  # New line for readability
}

# 10 Posts for User 1: Triwa Leearn (Education-themed)
create_post "$USER1" "The Power of Lifelong Learning" "In a rapidly changing world, committing to lifelong learning ensures personal and professional growth. Embrace new skills daily!"
create_post "$USER1" "Why Reading Books Matters" "Books open doors to new ideas and perspectives. Make reading a habit to expand your mind and creativity."
create_post "$USER1" "Online Courses: A Game Changer" "Platforms like Coursera and edX make education accessible to everyone. Start your learning journey today."
create_post "$USER1" "The Role of Teachers in Society" "Teachers shape the future by inspiring students. Their dedication deserves more recognition and support."
create_post "$USER1" "Overcoming Learning Challenges" "Everyone faces obstacles in learning. Persistence and the right strategies can turn weaknesses into strengths."
create_post "$USER1" "The Benefits of Group Study" "Studying with peers fosters collaboration and deeper understanding. Try it for your next exam prep."
create_post "$USER1" "Technology in Education" "From AI tutors to virtual classrooms, tech is revolutionizing how we learn. What's your favorite edtech tool?"
create_post "$USER1" "Building a Personal Library" "Curate books that inspire you. A personal library is a treasure trove of knowledge and wisdom."
create_post "$USER1" "The Joy of Learning Languages" "Mastering a new language connects you to cultures. Apps like Duolingo make it fun and easy."
create_post "$USER1" "Mentorship: Guiding the Next Generation" "Mentors provide invaluable advice. Seek one out or become one to pay it forward."

# 10 Posts for User 2: Red Trips (Travel-themed)
create_post "$USER2" "Adventures in the Red Desert" "Exploring vast sand dunes under the stars was unforgettable. Pack light and embrace the silence!"
create_post "$USER2" "Hidden Gems in Europe" "Beyond Paris, discover quaint villages in the Alps. Perfect for a peaceful getaway."
create_post "$USER2" "Backpacking Through Asia" "From bustling markets in Bangkok to serene temples in Kyoto, Asia offers endless wonders."
create_post "$USER2" "Road Trip Essentials" "Don't forget snacks, a good playlist, and a reliable map app for your next highway adventure."
create_post "$USER2" "Beach Escapes: My Favorites" "White sands of Bali or rocky shores of Greece? Both are paradise for relaxation."
create_post "$USER2" "Solo Travel Tips" "Traveling alone builds confidence. Stay safe by researching destinations and trusting your instincts."
create_post "$USER2" "Mountain Hiking Adventures" "Conquering peaks like Everest Base Camp tests your limits and rewards with stunning views."
create_post "$USER2" "Cultural Festivals Around the World" "Join Diwali in India or Carnival in Brazil for immersive cultural experiences."
create_post "$USER2" "Budget Travel Hacks" "Use hostels, local transport, and street food to explore more without breaking the bank."
create_post "$USER2" "Wildlife Safaris in Africa" "Spotting lions in the Serengeti is thrilling. Respect nature and support conservation efforts."

# 10 Posts for User 3: Gurt Swerr (Food and Culture-themed)
create_post "$USER3" "The Art of Homemade Yogurt" "Fermenting your own yogurt is simple and healthy. Add fruits for a delicious twist."
create_post "$USER3" "Sweet Treats from Around the World" "From French macarons to Japanese mochi, sweets tell cultural stories."
create_post "$USER3" "Exploring Street Food Delights" "Tacos in Mexico or falafel in the Middle East—street food is affordable and authentic."
create_post "$USER3" "Healthy Eating on a Budget" "Focus on seasonal veggies and grains. Meal prep saves time and money."
create_post "$USER3" "The Magic of Spices" "Turmeric, cumin, and cinnamon elevate any dish. Experiment in your kitchen!"
create_post "$USER3" "Traditional Family Recipes" "Passed down generations, these recipes connect us to our heritage. Share yours."
create_post "$USER3" "Vegetarian Dishes That Wow" "Try stuffed peppers or lentil soups for flavorful, meat-free meals."
create_post "$USER3" "Baking Bread at Home" "The smell of fresh bread is unbeatable. Knead, rise, bake—it's therapeutic."
create_post "$USER3" "Pairing Wine with Food" "Red with steak, white with fish. Simple rules for enhancing your dining experience."
create_post "$USER3" "Fusion Cuisine Ideas" "Blend Italian pasta with Asian flavors for exciting new tastes. Get creative!"