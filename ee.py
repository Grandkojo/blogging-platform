@api_view(['POST'])
def predict_disease(request):
    # Extract features (no PII stored)
    user = request.user
    age = request.data.get('age')
    gender = request.data.get('gender')
    locality = request.data.get('locality')
    symptoms = request.data.get('symptoms_description')
    
    # XGBoost prediction (via your ML service)
    prediction = model.predict(features)
    confidence_score = model.predict_proba(features)
    
    # SHAP explainability
    shap_values = explainer.shap_values(features)
    
    # Gemini RAG for natural language insights
    ai_insights = generate_gemini_insight(
        prediction, shap_values, symptoms
    )
    
    # Create prediction history record
    prediction_record = PredictionHistoryService.create_prediction(
        user=user,
        age=age,
        gender=gender,
        locality=locality,
        schedule_date=schedule_date,
        disease_type=disease_type,
        symptoms_description=symptoms,
        predicted_disease=prediction,
        confidence_score=confidence_score,
        ai_insights=ai_insights
    )
    
    return Response({
        'prediction_id': prediction_record.prediction_id,
        'predicted_disease': prediction,
        'confidence_score': confidence_score,
        'ai_insights': ai_insights,
        'status': 'pending'  # Awaits feedback
    })